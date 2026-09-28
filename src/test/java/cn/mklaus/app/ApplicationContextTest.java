package cn.mklaus.app;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * 上下文加载测试。
 *
 * <p>
 * 不需要数据库：它验证的是 Bean 装配（含 domain 接口 + infrastructure 实现）、
 * Mapper XML 解析、Controller 注册这些事情没坏。任何一处装配错误都会让这个测试红。
 *
 * @author klaus
 * @since 2026/9/28
 */
@SpringBootTest
class ApplicationContextTest {

    @Test
    void contextLoads() {
    }

}
