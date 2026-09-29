package cn.mklaus.app;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 实体字段 ↔ resultMap 列 ↔ 建表语句 三方对账（不需要数据库）。
 *
 * <p>
 * 这三处是**手工同步**的，而漏一处的后果分三种，实测过：
 *
 * <ul>
 * <li>实体加了字段、XML 没跟上：{@code User} 这类有**无参构造器**的实体被 MyBatis 静默忽略
 * ——值永远不落库、也查不出来，没有任何报错；</li>
 * <li>{@code Address} 这类只有全参构造器的实体则抛
 * {@code Constructor auto-mapping of 'Address(...)' failed}，报错和真因（漏字段）完全不搭，
 * HTTP 层只剩一个笼统的 {@code code 90000}；</li>
 * <li>XML 用了建表语句里不存在的列：要跑那条 SQL 时才炸。</li>
 * </ul>
 *
 * <p>
 * 与 {@link MapperStatementsTest} 分工：那个查"接口方法 ↔ XML statement"，这个查
 * "实体 ↔ 列"。两者都从仓库里的真实文件解析，新增实体 / mapper / 迁移脚本会自动纳入，不需要维护清单。
 *
 * <p>
 * 已知简化：建表语句按"一行一列"解析（本仓库的写法），列定义与关键字不能写在同一行。
 *
 * @author klaus
 * @since 2026/9/29
 */
class EntityMappingTest {

    private static final Path MAPPER_DIR = Path.of("src/main/resources/mapper");

    private static final Path MIGRATION_DIR = Path.of("src/main/resources/db/migration");

    /**
     * CREATE TABLE 到 ENGINE 之间是列定义。
     */
    private static final Pattern TABLE_DEFINITION = Pattern.compile(
        "CREATE TABLE IF NOT EXISTS\\s+`?(\\w+)`?\\s*\\((.*?)\\)\\s*ENGINE", Pattern.DOTALL);

    private static final Pattern RESULT_MAP = Pattern.compile(
        "<resultMap\\s[^>]*type=\"([^\"]+)\"[^>]*>(.*?)</resultMap>", Pattern.DOTALL);

    private static final Pattern MAPPING_LINE = Pattern.compile(
        "<(?:id|result)\\s+property=\"([^\"]+)\"\\s+column=\"([^\"]+)\"");

    private static final Pattern REFERENCED_TABLE = Pattern.compile(
        "(?:FROM|INTO|UPDATE)\\s+`?(\\w+)`?", Pattern.CASE_INSENSITIVE);

    private static final Pattern INSERT_COLUMNS = Pattern.compile(
        "INSERT\\s+INTO\\s+`?\\w+`?\\s*\\(([^)]*)\\)", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    private static final Pattern UPDATE_SET = Pattern.compile(
        "UPDATE\\s+`?\\w+`?\\s+SET\\s+(.*?)\\s+WHERE\\b", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    /**
     * 实体类被 {@code resultType} 直接映射（而不是显式 resultMap）。
     */
    private static final Pattern RESULT_TYPE_ENTITY = Pattern.compile(
        "resultType=\"(cn\\.mklaus\\.app\\.domain\\.[^\"]+)\"");

    /**
     * 不需要出现在 UPDATE 里的列：主键（自增）、归属 / 外键列（{@code *_id}）、审计时间列。
     *
     * <p>
     * 确实不该被更新的业务列（比如订单号），把名字加进这个正则并写清理由——这是**有意保留的例外出口**，
     * 不是遗漏。
     */
    private static final Pattern INSERT_ONLY_COLUMN = Pattern.compile(
        "(?i)^(.*_id|created_at|updated_at|created_by|updated_by)$");

    /**
     * 建表语句里不是列定义的行。
     */
    private static final Pattern NON_COLUMN_LINE = Pattern.compile(
        "(?i)^(PRIMARY|UNIQUE|KEY|INDEX|CONSTRAINT|\\)\\s*ENGINE|--).*");

    private static final Pattern SQL_FRAGMENT = Pattern.compile(
        "<sql\\s+id=\"([^\"]+)\"[^>]*>(.*?)</sql>", Pattern.DOTALL);

    /**
     * 片段引用的展开轮数上限（够嵌套用即可）。
     */
    private static final int MAX_FRAGMENT_DEPTH = 5;

    @Test
    void entityFieldsShouldMatchResultMapProperties() throws Exception {
        List<String> problems = new ArrayList<>();
        for (Map.Entry<Path, String> mapperXml : mapperXmlFiles().entrySet()) {
            String fileName = mapperXml.getKey().getFileName().toString();
            for (ResultMapDefinition definition : resultMaps(mapperXml.getValue())) {
                Set<String> fields = entityFieldNames(definition.entityType());
                difference(fields, definition.properties()).forEach(missing -> problems.add(fileName
                    + " 的 resultMap 漏了实体字段 " + missing + "：MyBatis 会静默忽略它（值不落库、也查不出来）"));
                difference(definition.properties(), fields).forEach(extra -> problems.add(fileName
                    + " 的 resultMap 映射了 " + definition.entityType() + " 上不存在的字段 " + extra));
            }
        }
        assertTrue(problems.isEmpty(), () -> String.join("\n", problems));
    }

    @Test
    void resultMapColumnsShouldExistInMigrations() throws Exception {
        Map<String, Set<String>> tableColumns = tableColumns();
        List<String> problems = new ArrayList<>();
        for (Map.Entry<Path, String> mapperXml : mapperXmlFiles().entrySet()) {
            String fileName = mapperXml.getKey().getFileName().toString();
            Set<String> tables = referencedTables(mapperXml.getValue(), tableColumns.keySet());
            assertFalse(tables.isEmpty(), fileName + " 里没解析到任何建表语句中的表：请检查 SQL 写法");

            for (ResultMapDefinition definition : resultMaps(mapperXml.getValue())) {
                for (String column : definition.columns()) {
                    boolean known = tables.stream().anyMatch(table -> tableColumns.get(table).contains(column));
                    if (!known) {
                        problems.add(fileName + " 的 resultMap 用了列 " + column + "，但 " + tables
                            + " 的建表语句里没有这一列（Flyway 迁移里补上，否则只在跑 SQL 时才炸）");
                    }
                }
            }
        }
        assertTrue(problems.isEmpty(), () -> String.join("\n", problems));
    }

    /**
     * 实体必须用显式 resultMap 映射，不能用 {@code resultType}。
     *
     * <p>
     * 两个原因，都是实测出来的：项目没开 {@code map-underscore-to-camel-case}，
     * {@code user_id} 这类列在 {@code resultType} 下会静默映射不上；
     * 而且本类的前两张对账（实体 ↔ resultMap、resultMap ↔ 建表列）会因为找不到 resultMap 而整条失效。
     */
    @Test
    void mappersShouldUseExplicitResultMapInsteadOfResultType() throws Exception {
        List<String> problems = new ArrayList<>();
        for (Map.Entry<Path, String> mapperXml : mapperXmlFiles().entrySet()) {
            Matcher matcher = RESULT_TYPE_ENTITY.matcher(mapperXml.getValue());
            while (matcher.find()) {
                problems.add(mapperXml.getKey().getFileName() + " 用 resultType=\"" + matcher.group(1)
                    + "\" 映射实体：请改回显式 resultMap（下划线列会静默映射不上，且对账会失效）");
            }
        }
        assertTrue(problems.isEmpty(), () -> String.join("\n", problems));
    }

    @Test
    void insertStatementsShouldCoverMappedColumns() throws Exception {
        Map<String, Set<String>> tableColumns = tableColumns();
        Map<String, Set<String>> autoIncrementColumns = autoIncrementColumns();
        List<String> problems = new ArrayList<>();
        for (Map.Entry<Path, String> mapperXml : mapperXmlFiles().entrySet()) {
            String fileName = mapperXml.getKey().getFileName().toString();
            Set<String> insertColumns = insertColumns(mapperXml.getValue());
            if (insertColumns.isEmpty()) {
                continue;
            }
            Set<String> tables = referencedTables(mapperXml.getValue(), tableColumns.keySet());
            for (ResultMapDefinition definition : resultMaps(mapperXml.getValue())) {
                for (String column : definition.columns()) {
                    boolean generated = tables.stream()
                        .anyMatch(table -> autoIncrementColumns.getOrDefault(table, Set.of()).contains(column));
                    if (!generated && !insertColumns.contains(column)) {
                        problems.add(fileName + " 的 INSERT 列清单里没有 " + column
                            + "：实体字段会被存成默认值 / NULL，而且不会有任何报错");
                    }
                }
            }
        }
        assertTrue(problems.isEmpty(), () -> String.join("\n", problems));
    }

    /**
     * 实体字段必须能通过 UPDATE 改到。
     *
     * <p>
     * 实测过的坑：字段留在 resultMap / INSERT 里、只从 UPDATE 的 SET 列表删掉，
     * 则本类前两张对账和真库测试**全都通过**——字段静默改不动（真库测试只断言了部分字段）。
     */
    @Test
    void updateStatementsShouldCoverMappedColumns() throws Exception {
        Map<String, Set<String>> tableColumns = tableColumns();
        Map<String, Set<String>> autoIncrementColumns = autoIncrementColumns();
        List<String> problems = new ArrayList<>();
        for (Map.Entry<Path, String> mapperXml : mapperXmlFiles().entrySet()) {
            String fileName = mapperXml.getKey().getFileName().toString();
            Set<String> updateColumns = updateColumns(mapperXml.getValue());
            if (updateColumns.isEmpty()) {
                continue;
            }
            Set<String> tables = referencedTables(mapperXml.getValue(), tableColumns.keySet());
            for (ResultMapDefinition definition : resultMaps(mapperXml.getValue())) {
                for (String column : definition.columns()) {
                    boolean generated = tables.stream()
                        .anyMatch(table -> autoIncrementColumns.getOrDefault(table, Set.of()).contains(column));
                    boolean insertOnly = INSERT_ONLY_COLUMN.matcher(column).matches();
                    if (!generated && !insertOnly && !updateColumns.contains(column)) {
                        problems.add(fileName + " 的 UPDATE 没更新列 " + column + "：实体这个字段改不动，"
                            + "而且不会有任何报错（若它确实不该被更新，加进 INSERT_ONLY_COLUMN 并说明理由）");
                    }
                }
            }
        }
        assertTrue(problems.isEmpty(), () -> String.join("\n", problems));
    }

    private static Map<Path, String> mapperXmlFiles() throws IOException {
        Map<Path, String> files = new LinkedHashMap<>();
        try (Stream<Path> paths = Files.list(MAPPER_DIR)) {
            for (Path path : paths.filter(Files::isRegularFile).sorted().toList()) {
                // 先展开 <include refid="..."/>：把列清单抽成 <sql> 片段是正常重构，
                // 不展开的话会被误判成"列不见了"
                files.put(path, expandFragments(Files.readString(path, StandardCharsets.UTF_8)));
            }
        }
        assertFalse(files.isEmpty(), "在 " + MAPPER_DIR + " 下没找到任何 mapper XML");
        return files;
    }

    /**
     * 把 {@code <include refid="x"/>} 换成对应 {@code <sql id="x">} 的内容（MyBatis 也是解析时展开）。
     */
    private static String expandFragments(String xml) {
        Map<String, String> fragments = new LinkedHashMap<>();
        Matcher matcher = SQL_FRAGMENT.matcher(xml);
        while (matcher.find()) {
            fragments.put(matcher.group(1), matcher.group(2));
        }

        String expanded = xml;
        for (int round = 0; round < MAX_FRAGMENT_DEPTH; round++) {
            String next = expanded;
            for (Map.Entry<String, String> fragment : fragments.entrySet()) {
                next = next.replace("<include refid=\"" + fragment.getKey() + "\"/>", fragment.getValue())
                    .replace("<include refid=\"" + fragment.getKey() + "\" />", fragment.getValue());
            }
            if (next.equals(expanded)) {
                return expanded;
            }
            expanded = next;
        }
        return expanded;
    }

    private static List<ResultMapDefinition> resultMaps(String xml) {
        List<ResultMapDefinition> definitions = new ArrayList<>();
        Matcher matcher = RESULT_MAP.matcher(xml);
        while (matcher.find()) {
            Set<String> properties = new LinkedHashSet<>();
            Set<String> columns = new LinkedHashSet<>();
            Matcher mapping = MAPPING_LINE.matcher(matcher.group(2));
            while (mapping.find()) {
                properties.add(mapping.group(1));
                columns.add(mapping.group(2));
            }
            definitions.add(new ResultMapDefinition(matcher.group(1), properties, columns));
        }
        return definitions;
    }

    private static Set<String> entityFieldNames(String entityType) throws ClassNotFoundException {
        Set<String> names = new LinkedHashSet<>();
        for (Field field : Class.forName(entityType).getDeclaredFields()) {
            // 跳过 static / 合成字段（jacoco 注入的 $jacocoData）与 transient 计算字段（不落库）
            if (!Modifier.isStatic(field.getModifiers()) && !field.isSynthetic()
                && !Modifier.isTransient(field.getModifiers())) {
                names.add(field.getName());
            }
        }
        return names;
    }

    private static Set<String> referencedTables(String xml, Set<String> knownTables) {
        Set<String> tables = new LinkedHashSet<>();
        Matcher matcher = REFERENCED_TABLE.matcher(xml);
        while (matcher.find()) {
            String table = matcher.group(1);
            if (knownTables.contains(table)) {
                tables.add(table);
            }
        }
        return tables;
    }

    private static Set<String> insertColumns(String xml) {
        Set<String> columns = new LinkedHashSet<>();
        Matcher matcher = INSERT_COLUMNS.matcher(xml);
        while (matcher.find()) {
            for (String column : matcher.group(1).split(",")) {
                columns.add(column.trim().replace("`", ""));
            }
        }
        return columns;
    }

    private static Set<String> updateColumns(String xml) {
        Set<String> columns = new LinkedHashSet<>();
        Matcher matcher = UPDATE_SET.matcher(xml);
        while (matcher.find()) {
            for (String assignment : matcher.group(1).split(",")) {
                int equals = assignment.indexOf('=');
                if (equals > 0) {
                    columns.add(assignment.substring(0, equals).trim().replace("`", ""));
                }
            }
        }
        return columns;
    }

    private static Map<String, Set<String>> tableColumns() throws IOException {
        Map<String, Set<String>> tables = new LinkedHashMap<>();
        for (String ddl : migrationScripts()) {
            Matcher matcher = TABLE_DEFINITION.matcher(ddl);
            while (matcher.find()) {
                tables.put(matcher.group(1), columnNames(matcher.group(2)));
            }
        }
        assertFalse(tables.isEmpty(), "在 " + MIGRATION_DIR + " 下没解析到任何建表语句");
        return tables;
    }

    private static Map<String, Set<String>> autoIncrementColumns() throws IOException {
        Map<String, Set<String>> generated = new LinkedHashMap<>();
        for (String ddl : migrationScripts()) {
            Matcher matcher = TABLE_DEFINITION.matcher(ddl);
            while (matcher.find()) {
                Set<String> columns = new LinkedHashSet<>();
                for (String line : matcher.group(2).split("\n")) {
                    if (line.toUpperCase().contains("AUTO_INCREMENT")) {
                        columns.addAll(columnNames(line));
                    }
                }
                generated.put(matcher.group(1), columns);
            }
        }
        return generated;
    }

    private static Set<String> columnNames(String ddlBody) {
        Set<String> columns = new LinkedHashSet<>();
        for (String rawLine : ddlBody.split("\n")) {
            String line = rawLine.trim();
            if (line.isEmpty() || NON_COLUMN_LINE.matcher(line).matches()) {
                continue;
            }
            Matcher name = Pattern.compile("^`?(\\w+)`?").matcher(line);
            if (name.find()) {
                columns.add(name.group(1));
            }
        }
        return columns;
    }

    private static List<String> migrationScripts() throws IOException {
        List<String> scripts = new ArrayList<>();
        try (Stream<Path> paths = Files.list(MIGRATION_DIR)) {
            for (Path path : paths.filter(Files::isRegularFile).sorted().toList()) {
                scripts.add(Files.readString(path, StandardCharsets.UTF_8));
            }
        }
        return scripts;
    }

    private static Set<String> difference(Set<String> left, Set<String> right) {
        Set<String> result = new LinkedHashSet<>(left);
        result.removeAll(right);
        return result;
    }

    /**
     * 一份 resultMap 的映射关系。
     *
     * @param entityType 映射的实体类名
     * @param properties 映射到的实体属性
     * @param columns    映射用到的数据库列
     */
    private record ResultMapDefinition(String entityType, Set<String> properties, Set<String> columns) {
    }

}
