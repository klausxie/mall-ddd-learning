package cn.mklaus.app;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.Configuration;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Mapper 接口方法 与 XML statement 的对账测试。
 *
 * <p>
 * 不需要数据库就能发现两类错误：
 * 接口加了方法但忘了写 SQL（调用时才报 Invalid bound statement）、
 * XML 里写了接口不存在的语句（拼错方法名）。
 *
 * @author klaus
 * @since 2026/9/28
 */
class MapperStatementsTest {

    /**
     * 自动发现所有 {@code *Mapper} 接口。
     *
     * <p>
     * **不要改回手写清单**：手写清单在"新增第 N 个 Mapper"时不会自动纳入对账，
     * 而那一刻恰恰最容易出现"接口加了方法、XML 忘了写"。
     */
    private static final List<Class<?>> MAPPERS = new ClassFileImporter()
        .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
        .importPackages(Application.class.getPackageName())
        .stream()
        .filter(javaClass -> javaClass.isInterface() && javaClass.getSimpleName().endsWith("Mapper"))
        .map(JavaClass::reflect)
        .toList();

    /**
     * 与 application.yaml 里 mybatis-plus.type-handlers-package 保持一致。
     */
    private static final String TYPE_HANDLER_PACKAGE = "cn.mklaus.app.infrastructure.persistence";

    @Test
    void mapperMethodsAndXmlStatementsShouldMatchOneToOne() throws Exception {
        assertFalse(MAPPERS.isEmpty(), "没有扫描到任何 *Mapper 接口：自动发现逻辑坏了，本测试会变成空转");

        Configuration configuration = loadMapperXml();

        List<String> problems = new ArrayList<>();
        for (Class<?> mapper : MAPPERS) {
            Set<String> methods = new LinkedHashSet<>();
            for (Method method : mapper.getDeclaredMethods()) {
                methods.add(mapper.getName() + "." + method.getName());
            }

            Set<String> statements = new LinkedHashSet<>();
            for (String name : configuration.getMappedStatementNames()) {
                if (name.startsWith(mapper.getName() + ".")) {
                    statements.add(name);
                }
            }

            methods.stream()
                .filter(name -> !statements.contains(name))
                .map(name -> "缺少 XML 语句: " + name)
                .forEach(problems::add);
            statements.stream()
                .filter(name -> !methods.contains(name))
                .map(name -> "XML 语句没有对应方法: " + name)
                .forEach(problems::add);
        }

        assertTrue(problems.isEmpty(), () -> String.join("\n", problems));
    }

    private static Configuration loadMapperXml() throws Exception {
        Configuration configuration = new Configuration();
        // 与应用配置保持一致：application.yaml 的 mybatis-plus.type-handlers-package
        // 若这里不注册，值对象（如 Mobile）会报 "No typehandler found for property"
        configuration.getTypeHandlerRegistry().register(TYPE_HANDLER_PACKAGE);
        for (Class<?> mapper : MAPPERS) {
            configuration.addMapper(mapper);

            String resource = "mapper/" + mapper.getSimpleName() + ".xml";
            try (InputStream in = openMapperXml(resource)) {
                new XMLMapperBuilder(in, configuration, resource, configuration.getSqlFragments()).parse();
            }
        }
        return configuration;
    }

    /**
     * 读取 Mapper XML。
     *
     * <p>
     * 注意 {@code Resources.getResourceAsStream} 找不到资源时是**抛 IOException**、不是返回 null，
     * 所以必须显式接住，否则"忘了建 XML"只会得到一句 MyBatis 的
     * {@code Could not find resource mapper/XxxMapper.xml}，看不到该往哪放。
     */
    private static InputStream openMapperXml(String resource) {
        try {
            return Resources.getResourceAsStream(resource);
        } catch (IOException e) {
            throw new IllegalStateException("缺少 XML 文件: src/main/resources/" + resource
                + "（每个 *Mapper 接口都要配一份同名 XML，否则运行到该语句时才报 Invalid bound statement）", e);
        }
    }

}
