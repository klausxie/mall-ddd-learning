package cn.mklaus.app.support;

import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 真库测试的**唯一开关**：数据源配了密码就跑，没配就跳过（原因打进日志）。
 *
 * <p>
 * 不用 {@code @EnabledIfEnvironmentVariable("MALL_DB_PASSWORD")} 的原因：那条路只在"用环境变量配数据源"
 * 时成立；凭证写在 {@code application-local.yaml} 并用 profile 跑时，数据源连得上、测试却被静默跳过，
 * 表现为"覆盖率 44% &lt; 80%"这种看不出真因的报错。见 {@link DatabaseConfiguredCondition}。
 *
 * @author klaus
 * @since 2026/9/29
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@ExtendWith(DatabaseConfiguredCondition.class)
public @interface EnabledIfDatabaseConfigured {
}
