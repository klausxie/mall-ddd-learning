package cn.mklaus.app.support;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记"这个测试需要真库"。带它的测试会自动拿到一个数据源，三级降级见
 * {@link RealDatabaseProvisioner}：显式配置 → Docker 里的 MySQL 容器 → H2(MODE=MySQL) 快速通道。
 *
 * <p>
 * 之所以还留一个标记注解、而不是让所有 {@code @SpringBootTest} 都置备：不需要库的测试
 * （如 {@code ApplicationContextTest}）不该为了探测 Docker 或拉镜像而变慢。
 *
 * @author klaus
 * @since 2026/9/29
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequiresRealDatabase {
}
