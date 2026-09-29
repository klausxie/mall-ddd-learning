package cn.mklaus.app.support;

import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 真库测试的**唯一开关**：数据源配了密码就跑，没配就跳过（跳过原因会打进日志）。
 *
 * <p>
 * 为什么不能直接用 {@code @EnabledIfEnvironmentVariable(named = "MALL_DB_PASSWORD")}：
 * 那条路只在"用环境变量配数据源"时成立；一旦凭证写在 {@code application-local.yaml} 并用
 * {@code --spring.profiles.active=local} 跑，数据源连得上、测试却被静默跳过，最后表现为
 * "覆盖率 44% &lt; 80%"这种看不出真因的报错。
 *
 * <p>
 * 这里改成让开关跟着**解析后的数据源配置**走，两种配法等价：
 *
 * <pre>
 * MALL_DB_PASSWORD=xxx ./mvnw clean verify -Pcoverage-check
 * MALL_DB_PASSWORD=xxx ./mvnw clean verify -Pcoverage-check -Dspring.profiles.active=local
 * </pre>
 *
 * @author klaus
 * @since 2026/9/29
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@ExtendWith(DatabaseConfiguredCondition.class)
public @interface EnabledIfDatabaseConfigured {
}
