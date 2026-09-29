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
 * 直接崩，轮不到跳过），那时拿不到 Spring 的 {@code Environment}。所以这里按**与应用相同的优先级**
 * 自己解析一遍：环境变量 / 系统属性（{@link StandardEnvironment} 自带，含
 * {@code SPRING_DATASOURCE_PASSWORD} 这类松散绑定）&gt; 各激活 profile 的
 * {@code application-<profile>.yaml} &gt; {@code application.yaml}（{@code ${MALL_DB_PASSWORD:}} 占位符
 * 也由它统一解析）。
 *
 * <p>
 * 效果：
 *
 * <ul>
 * <li>配了 {@code MALL_DB_PASSWORD} → 跑；</li>
 * <li>任意 profile（{@code local} / {@code dev} / …）的 {@code application-<profile>.yaml} 填了密码 → 也跑；</li>
 * <li>都没有 → 跳过，并把原因写进日志与 skip reason。</li>
 * </ul>
 *
 * <p>
 * 有意保留的简化：profile 只从环境变量 / 系统属性判定（即 {@code -Dspring.profiles.active=dev}
 * 或 {@code SPRING_PROFILES_ACTIVE=dev}），不支持在 YAML 内部声明 {@code spring.profiles.active}；多文档 YAML 与
 * {@code spring.config.activate.on-profile} 同理。
 *
 * @author klaus
 * @since 2026/9/29
 */
@Slf4j
public class DatabaseConfiguredCondition implements ExecutionCondition {

    private static final String DATASOURCE_PASSWORD = "spring.datasource.password";

    private static final String ACTIVE_PROFILES = "spring.profiles.active";

    private static final String BASE_CONFIG = "application.yaml";

    @Override
    public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
        String password = resolveDataSourcePassword();
        if (password == null || password.isBlank()) {
            String reason = "真库测试跳过：没有解析到 " + DATASOURCE_PASSWORD + "。两种等价的配法——"
                + "① 给环境变量 MALL_DB_PASSWORD；② 在 application-<profile>.yaml 里填密码并激活该 profile"
                + "（如 application-local.yaml + -Dspring.profiles.active=local）。见 README「验证」一节。";
            log.warn(reason);
            return ConditionEvaluationResult.disabled(reason);
        }
        // 只打长度，绝不把密码本身写进日志
        return ConditionEvaluationResult.enabled("数据源密码已配置（" + password.length() + " 字符），真库测试执行");
    }

    /**
     * 按与应用一致的优先级解析数据源密码：环境变量 / 系统属性 &gt; application-&lt;profile&gt;.yaml &gt; application.yaml。
     *
     * <p>
     * 每个激活的 profile 都试一遍（不写死 {@code local}）：否则换个 profile 名就会"有密码却静默跳过"。
     * 多个 profile 时按 Spring 的"后者优先"语义，倒序加入。
     */
    static String resolveDataSourcePassword() {
        StandardEnvironment environment = new StandardEnvironment();
        List<String> profiles = activeProfiles(environment.getProperty(ACTIVE_PROFILES));
        // addLast = 放到最低优先级：先加低优先级的，最后加 application.yaml
        for (int index = profiles.size() - 1; index >= 0; index--) {
            addYaml(environment, "application-" + profiles.get(index) + ".yaml");
        }
        addYaml(environment, BASE_CONFIG);
        return environment.getProperty(DATASOURCE_PASSWORD);
    }

    private static List<String> activeProfiles(String activeProfiles) {
        if (activeProfiles == null || activeProfiles.isBlank()) {
            return List.of();
        }
        return Arrays.stream(activeProfiles.split(","))
            .map(String::trim)
            .filter(profile -> !profile.isEmpty())
            .toList();
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
