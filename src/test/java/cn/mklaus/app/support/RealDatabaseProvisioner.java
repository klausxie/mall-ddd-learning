package cn.mklaus.app.support;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StringUtils;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.mysql.MySQLContainer;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 给真库测试置备数据源，三级降级：
 *
 * <ol>
 * <li>**显式配置**（环境变量 {@code MALL_DB_*} / {@code SPRING_DATASOURCE_*}，或激活 profile 的
 * {@code application-<profile>.yaml} 里填了密码）→ 用它，什么都不做；</li>
 * <li>否则 **Docker 可用** → 起一个 {@code mysql:8.0} 容器（整个 JVM 共用一个，由 Testcontainers 回收）；</li>
 * <li>否则 **H2 的 MySQL 兼容模式**快速通道。</li>
 * </ol>
 *
 * <p>
 * 第 3 级是"没有 Docker 也能让默认循环覆盖到应用层"的兜底。它跑的是同一份
 * {@code db/migration/V1__init_schema.sql}（实测 H2 2.x 的 MySQL 模式能原样应用），
 * 但**引擎不是 MySQL**，所以选到这一级时会打一条 WARN：真 MySQL 由 CI 的 service container 负责校验。
 *
 * <p>
 * 只对带 {@link RequiresRealDatabase} 的测试生效，见 {@link RealDatabaseContextCustomizerFactory}。
 *
 * @author klaus
 * @since 2026/9/29
 */
@Slf4j
final class RealDatabaseProvisioner {

    private static final String H2_URL = "jdbc:h2:mem:mall;MODE=MySQL;DATABASE_TO_LOWER=TRUE;CASE_INSENSITIVE_IDENTIFIERS=TRUE;DB_CLOSE_DELAY=-1";

    /** 显式配置的判据：这些环境变量 / 系统属性任意一个非空。 */
    private static final List<String> EXPLICIT_KEYS = List.of("MALL_DB_URL", "MALL_DB_USERNAME", "MALL_DB_PASSWORD",
        "SPRING_DATASOURCE_URL", "SPRING_DATASOURCE_USERNAME", "SPRING_DATASOURCE_PASSWORD");

    private static final String ACTIVE_PROFILES = "spring.profiles.active";

    private static final String DATASOURCE_URL = "spring.datasource.url";

    /** 容器只起一次，多个测试类共用。 */
    private static MySQLContainer container;

    private RealDatabaseProvisioner() {
    }

    static void provision(ConfigurableEnvironment environment) {
        if (explicitlyConfigured(environment)) {
            log.info("真库测试：使用显式配置的数据源（环境变量或 profile 文件）");
            return;
        }
        if (dockerAvailable()) {
            MySQLContainer mysql = startContainer();
            Map<String, Object> properties = new LinkedHashMap<>();
            properties.put(DATASOURCE_URL, mysql.getJdbcUrl());
            properties.put("spring.datasource.username", mysql.getUsername());
            properties.put("spring.datasource.password", mysql.getPassword());
            properties.put("spring.datasource.driver-class-name", mysql.getDriverClassName());
            addFirst(environment, properties);
            log.info("真库测试：已启动 MySQL 容器 {}", mysql.getDockerImageName());
            return;
        }

        Map<String, Object> h2 = new LinkedHashMap<>();
        h2.put(DATASOURCE_URL, H2_URL);
        h2.put("spring.datasource.driver-class-name", "org.h2.Driver");
        h2.put("spring.datasource.username", "sa");
        h2.put("spring.datasource.password", "sa");
        addFirst(environment, h2);
        log.warn("真库测试：本机没有可用的 Docker，改用 H2(MODE=MySQL) 快速通道——用例能全跑，但引擎不是 MySQL；"
            + "真 MySQL 由 CI 的 service container 校验");
    }

    private static boolean explicitlyConfigured(ConfigurableEnvironment environment) {
        for (String key : EXPLICIT_KEYS) {
            if (StringUtils.hasText(environment.getProperty(key))) {
                return true;
            }
        }
        // profile 文件里的密码也算显式配置（如 application-local.yaml）
        StandardEnvironment resolver = new StandardEnvironment();
        List<String> profiles = activeProfiles(environment.getProperty(ACTIVE_PROFILES));
        for (int index = profiles.size() - 1; index >= 0; index--) {
            addYaml(resolver, "application-" + profiles.get(index) + ".yaml");
        }
        addYaml(resolver, "application.yaml");
        return StringUtils.hasText(resolver.getProperty("spring.datasource.password"));
    }

    private static List<String> activeProfiles(String activeProfiles) {
        if (!StringUtils.hasText(activeProfiles)) {
            return List.of();
        }
        return Arrays.stream(activeProfiles.split(",")).map(String::trim).filter(profile -> !profile.isEmpty())
            .toList();
    }

    private static boolean dockerAvailable() {
        try {
            return DockerClientFactory.instance().isDockerAvailable();
        } catch (RuntimeException e) {
            log.info("真库测试：Docker 探测失败（{}），降级到 H2 快速通道", e.getMessage());
            return false;
        }
    }

    private static synchronized MySQLContainer startContainer() {
        if (container == null) {
            container = new MySQLContainer("mysql:8.0").withDatabaseName("mall").withUsername("klaus")
                .withPassword("klaus");
            container.start();
        }
        return container;
    }

    private static void addFirst(ConfigurableEnvironment environment, Map<String, Object> properties) {
        environment.getPropertySources().addFirst(new MapPropertySource(RealDatabaseProvisioner.class.getName(),
            properties));
    }

    private static void addYaml(StandardEnvironment environment, String location) {
        ClassPathResource resource = new ClassPathResource(location);
        if (!resource.exists()) {
            return;
        }
        try {
            for (PropertySource<?> source : new YamlPropertySourceLoader().load(location, resource)) {
                environment.getPropertySources().addLast(source);
            }
        } catch (IOException e) {
            throw new IllegalStateException("解析 " + location + " 失败", e);
        }
    }

}
