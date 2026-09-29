package cn.mklaus.app.support;

import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.test.context.ContextConfigurationAttributes;
import org.springframework.test.context.ContextCustomizer;
import org.springframework.test.context.ContextCustomizerFactory;

import java.util.List;

/**
 * 把 {@link RequiresRealDatabase} 变成"自动挂一个数据源"：只对带该标记的测试类生效。
 *
 * <p>
 * 写成 {@code ContextCustomizerFactory}（经 {@code META-INF/spring.factories} 注册）而不是在每个测试类上写
 * {@code @DynamicPropertySource}：规则只有一份，新增真库测试时只要加个注解，不用再抄三行样板。
 *
 * @author klaus
 * @since 2026/9/29
 */
public class RealDatabaseContextCustomizerFactory implements ContextCustomizerFactory {

    @Override
    public ContextCustomizer createContextCustomizer(Class<?> testClass,
        List<ContextConfigurationAttributes> configAttributes) {
        if (!AnnotatedElementUtils.isAnnotated(testClass, RequiresRealDatabase.class)) {
            return null;
        }
        return (context, mergedConfig) -> RealDatabaseProvisioner.provision(context.getEnvironment());
    }

}
