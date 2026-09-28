package cn.mklaus.app;

import cn.mklaus.app.domain.product.ProductMapper;
import cn.mklaus.app.domain.user.AddressMapper;
import cn.mklaus.app.domain.user.UserMapper;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.Configuration;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

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

    private static final List<Class<?>> MAPPERS = List.of(UserMapper.class, AddressMapper.class, ProductMapper.class);

    @Test
    void mapperMethodsAndXmlStatementsShouldMatchOneToOne() throws Exception {
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
        for (Class<?> mapper : MAPPERS) {
            configuration.addMapper(mapper);

            String resource = "mapper/" + mapper.getSimpleName() + ".xml";
            try (InputStream in = Resources.getResourceAsStream(resource)) {
                new XMLMapperBuilder(in, configuration, resource, configuration.getSqlFragments()).parse();
            }
        }
        return configuration;
    }

}
