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
 * 实体字段 ↔ resultMap / INSERT / UPDATE ↔ 建表列 对账（不需要数据库）。
 *
 * <p>
 * 这几处是**手工同步**的，漏一处的后果实测分三种：{@code User} 这类有无参构造器的实体会被 MyBatis
 * 静默忽略（值不落库、也查不出来）；{@code Address} 这类只有全参构造器的会抛
 * {@code Constructor auto-mapping ... failed}（报错和真因完全不搭）；XML 用了建表语句里不存在的列，
 * 则要跑到那条 SQL 才炸。
 *
 * <p>
 * 与 {@link MapperStatementsTest} 分工：那个查"接口方法 ↔ XML statement"，这个查"实体 ↔ 列"。
 * 两者都从仓库里的真实文件解析，新增实体 / mapper / 迁移脚本会自动纳入，不需要维护清单。
 * 已知简化：建表语句按"一行一列"解析。
 *
 * @author klaus
 * @since 2026/9/29
 */
class EntityMappingTest {

    private static final Path MAPPER_DIR = Path.of("src/main/resources/mapper");

    private static final Path MIGRATION_DIR = Path.of("src/main/resources/db/migration");

    /** CREATE TABLE 到 ENGINE 之间是列定义。 */
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

    /** 实体被 {@code resultType} 直接映射（而不是显式 resultMap）。 */
    private static final Pattern RESULT_TYPE_ENTITY = Pattern.compile(
        "resultType=\"(cn\\.mklaus\\.app\\.domain\\.[^\"]+)\"");

    /**
     * 不需要出现在 UPDATE 里的列：自增主键、归属 / 外键列（{@code *_id}）、审计列。
     * 确实不该被更新的业务列，把名字加进这个正则并写清理由——这是有意保留的例外出口。
     */
    private static final Pattern INSERT_ONLY_COLUMN = Pattern.compile(
        "(?i)^(.*_id|created_at|updated_at|created_by|updated_by)$");

    /** 建表语句里不是列定义的行。 */
    private static final Pattern NON_COLUMN_LINE = Pattern.compile(
        "(?i)^(PRIMARY|UNIQUE|KEY|INDEX|CONSTRAINT|\\)\\s*ENGINE|--).*");

    private static final Pattern COLUMN_NAME = Pattern.compile("^`?(\\w+)`?");

    private static final Pattern SQL_FRAGMENT = Pattern.compile(
        "<sql\\s+id=\"([^\"]+)\"[^>]*>(.*?)</sql>", Pattern.DOTALL);

    private static final String AUTO_INCREMENT = "AUTO_INCREMENT";

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
        Map<String, TableDefinition> tables = tables();
        List<String> problems = new ArrayList<>();
        for (Map.Entry<Path, String> mapperXml : mapperXmlFiles().entrySet()) {
            String fileName = mapperXml.getKey().getFileName().toString();
            Set<String> usedTables = referencedTables(mapperXml.getValue(), tables.keySet());
            assertFalse(usedTables.isEmpty(), fileName + " 里没解析到任何建表语句中的表：请检查 SQL 写法");

            for (ResultMapDefinition definition : resultMaps(mapperXml.getValue())) {
                for (String column : definition.columns()) {
                    boolean known = usedTables.stream()
                        .anyMatch(table -> tables.get(table).columns().contains(column));
                    if (!known) {
                        problems.add(fileName + " 的 resultMap 用了列 " + column + "，但 " + usedTables
                            + " 的建表语句里没有这一列（Flyway 迁移里补上，否则只在跑 SQL 时才炸）");
                    }
                }
            }
        }
        assertTrue(problems.isEmpty(), () -> String.join("\n", problems));
    }

    /**
     * 实体必须用显式 resultMap 映射。项目没开 {@code map-underscore-to-camel-case}，用 {@code resultType}
     * 会让 {@code user_id} 这类列静默映射不上，而且本类的前两张对账会因为找不到 resultMap 而整条失效。
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

    /**
     * 实体字段必须既能存进去（INSERT）也能改到（UPDATE）。实测过的坑：字段留在 resultMap / INSERT 里、
     * 只从 UPDATE 的 SET 列表删掉，则本类其它对账和真库测试全都通过——字段静默改不动。
     */
    @Test
    void writeStatementsShouldCoverMappedColumns() throws Exception {
        Map<String, TableDefinition> tables = tables();
        List<String> problems = new ArrayList<>();
        for (Map.Entry<Path, String> mapperXml : mapperXmlFiles().entrySet()) {
            String fileName = mapperXml.getKey().getFileName().toString();
            Set<String> inserted = statementColumns(INSERT_COLUMNS, mapperXml.getValue());
            Set<String> updated = statementColumns(UPDATE_SET, mapperXml.getValue());
            Set<String> usedTables = referencedTables(mapperXml.getValue(), tables.keySet());

            for (ResultMapDefinition definition : resultMaps(mapperXml.getValue())) {
                for (String column : definition.columns()) {
                    boolean generated = usedTables.stream()
                        .anyMatch(table -> tables.get(table).autoIncrement().contains(column));
                    if (!generated && !inserted.isEmpty() && !inserted.contains(column)) {
                        problems.add(fileName + " 的 INSERT 列清单里没有 " + column
                            + "：字段会被存成默认值 / NULL，而且不会有任何报错");
                    }
                    if (!generated && !updated.isEmpty() && !INSERT_ONLY_COLUMN.matcher(column).matches()
                        && !updated.contains(column)) {
                        problems.add(fileName + " 的 UPDATE 没更新列 " + column + "：字段改不动，也不会有任何报错"
                            + "（若它确实不该被更新，加进 INSERT_ONLY_COLUMN 并说明理由）");
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
                // 先展开 <include refid="..."/>：把列清单抽成 <sql> 片段是正常重构，不展开会误判成"列不见了"
                files.put(path, expandFragments(Files.readString(path, StandardCharsets.UTF_8)));
            }
        }
        assertFalse(files.isEmpty(), "在 " + MAPPER_DIR + " 下没找到任何 mapper XML");
        return files;
    }

    /** 把 {@code <include refid="x"/>} 换成对应 {@code <sql id="x">} 的内容（MyBatis 也是解析时展开）。 */
    private static String expandFragments(String xml) {
        Map<String, String> fragments = new LinkedHashMap<>();
        Matcher matcher = SQL_FRAGMENT.matcher(xml);
        while (matcher.find()) {
            fragments.put(matcher.group(1), matcher.group(2));
        }

        String expanded = xml;
        for (int round = 0; round < 5; round++) {
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
            // 跳过 static / 合成字段（jacoco 的 $jacocoData）与 transient 计算字段（不落库）
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
            if (knownTables.contains(matcher.group(1))) {
                tables.add(matcher.group(1));
            }
        }
        return tables;
    }

    /** INSERT 取列清单、UPDATE 取 SET 子句里等号左边的列名。 */
    private static Set<String> statementColumns(Pattern pattern, String xml) {
        Set<String> columns = new LinkedHashSet<>();
        Matcher matcher = pattern.matcher(xml);
        while (matcher.find()) {
            for (String part : matcher.group(1).split(",")) {
                int equals = part.indexOf('=');
                columns.add((equals > 0 ? part.substring(0, equals) : part).trim().replace("`", ""));
            }
        }
        return columns;
    }

    private static Map<String, TableDefinition> tables() throws IOException {
        Map<String, TableDefinition> tables = new LinkedHashMap<>();
        try (Stream<Path> paths = Files.list(MIGRATION_DIR)) {
            for (Path path : paths.filter(Files::isRegularFile).sorted().toList()) {
                Matcher matcher = TABLE_DEFINITION.matcher(Files.readString(path, StandardCharsets.UTF_8));
                while (matcher.find()) {
                    tables.put(matcher.group(1), tableDefinition(matcher.group(2)));
                }
            }
        }
        assertFalse(tables.isEmpty(), "在 " + MIGRATION_DIR + " 下没解析到任何建表语句");
        return tables;
    }

    private static TableDefinition tableDefinition(String ddlBody) {
        Set<String> columns = new LinkedHashSet<>();
        Set<String> autoIncrement = new LinkedHashSet<>();
        for (String rawLine : ddlBody.split("\n")) {
            String line = rawLine.trim();
            if (line.isEmpty() || NON_COLUMN_LINE.matcher(line).matches()) {
                continue;
            }
            Matcher name = COLUMN_NAME.matcher(line);
            if (name.find()) {
                columns.add(name.group(1));
                if (line.toUpperCase().contains(AUTO_INCREMENT)) {
                    autoIncrement.add(name.group(1));
                }
            }
        }
        return new TableDefinition(columns, autoIncrement);
    }

    private static Set<String> difference(Set<String> left, Set<String> right) {
        Set<String> result = new LinkedHashSet<>(left);
        result.removeAll(right);
        return result;
    }

    /** 一份 resultMap 的映射关系；以及一张表的列与自增列。 */
    private record ResultMapDefinition(String entityType, Set<String> properties, Set<String> columns) {
    }

    private record TableDefinition(Set<String> columns, Set<String> autoIncrement) {
    }

}
