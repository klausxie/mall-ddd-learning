package cn.mklaus.app.support;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.env.PropertySource;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * 判定真库测试要不要跑：解析 {@code spring.datasource.password}，非空就跑。
 *
 * <p>
 * JUnit 的条件在 **Spring 容器启动之前**求值（必须如此：没库时容器会因为 Flyway / 连接池 fail-fast
 * 直接崩，轮不到跳过），那时拿不到 Spring 的 {@code Environment}。所以这里自己按**与应用相同的优先级**
 * 解析一遍数据源配置：
 *
 * <pre>
 * 系统属性 / 环境变量  &gt;  application-local.yaml（仅 local profile）  &gt;  application.yaml
 * </pre>
 *
 * 环境变量这一层是 Spring 的 {@link StandardEnvironment} 自带的（含
 * {@code SPRING_DATASOURCE_PASSWORD} 这类松散绑定），{@code application.yaml} 里的
 * {@code ${MALL_DB_PASSWORD:}} 占位符也由它统一解析，因此：
 *
 * <ul>
 * <li>配了 {@code MALL_DB_PASSWORD} → 跑；</li>
 * <li>用 local profile + {@code application-local.yaml} 填了密码 → 也跑；</li>
 * <li>两者都没有 → 跳过，并把原因写进日志与 skip reason。</li>
 * </ul>
 *
 * <p>
 * 有意保留的简化：profile 只从环境变量 / 系统属性判定（即 {@code -Dspring.profiles.active=local}
 * 或 {@code SPRING_PROFILES_ACTIVE=local}，项目文档里就是这么用的），不支持在 YAML 内部声明
 * {@code spring.profiles.active}；多文档 YAML 与 {@code spring.config.activate.on-profile} 同理。
 *
 * @author klaus
 * @since 2026/9/29
 */
@Slf4j
public class DatabaseConfiguredCondition implements ExecutionCondition {

    private static final String DATASOURCE_PASSWORD = "spring.datasource.password";

    private static final String ACTIVE_PROFILES = "spring.profiles.active";

    private static final String LOCAL_PROFILE = "local";

    private static final String LOCAL_CONFIG = "application-local.yaml";

    private static final String BASE_CONFIG = "application.yaml";

    @Override
    public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
        String password = resolveDataSourcePassword();
        if (password == null || password.isBlank()) {
            String reason = "真库测试跳过：没有解析到 " + DATASOURCE_PASSWORD + "。两种等价的配法——"
                + "① 给环境变量 MALL_DB_PASSWORD；② 用 local profile 的 application-local.yaml。见 README「验证」一节。";
            log.warn(reason);
            return ConditionEvaluationResult.disabled(reason);
        }
        // 只打长度，绝不把密码本身写进日志
        return ConditionEvaluationResult.enabled("数据源密码已配置（" + password.length() + " 字符），真库测试执行");
    }

    /**
     * 按与应用一致的优先级解析数据源密码。
     */
    static String resolveDataSourcePassword() {
        StandardEnvironment environment = new StandardEnvironment();
        // addLast = 放到最低优先级，所以"先加 local、再加 base"，让 local 压过 base、环境变量压过两者
        if (hasLocalProfile(environment.getProperty(ACTIVE_PROFILES))) {
            addYaml(environment, LOCAL_CONFIG);
        }
        addYaml(environment, BASE_CONFIG);
        return environment.getProperty(DATASOURCE_PASSWORD);
    }

    private static boolean hasLocalProfile(String activeProfiles) {
        if (activeProfiles == null || activeProfiles.isBlank()) {
            return false;
        }
        return Arrays.stream(activeProfiles.split(","))
            .map(String::trim)
            .anyMatch(LOCAL_PROFILE::equals);
    }

    private static void addYaml(StandardEnvironment environment, String location) {
        ClassPathResource resource = new ClassPathResource(location);
        if (!resource.exists()) {
            return;
        }
        try {
            List<PropertySource<?>> sources = new YamlPropertySourceLoader().load(location, resource);
            sources.forEach(environment.getPropertySources()::addLast);
        } catch (IOException e) {
            throw new IllegalStateException("解析 " + location + " 失败", e);
        }
    }

}
