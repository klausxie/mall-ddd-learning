package cn.mklaus.app.web;

import cn.mklaus.app.domain.common.PasswordHasher;
import cn.mklaus.app.domain.user.Mobile;
import cn.mklaus.app.domain.user.UserMapper;
import cn.mklaus.app.support.RequiresRealDatabase;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestConstructor;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 黄金路径：注册 → 校验验证码 → 成年规格 → 密码哈希入库 → 按年龄送积分 → 统一响应。
 *
 * <p>
 * 需要真实数据库（表由 Flyway 迁移创建），数据源配了密码就跑，没配则跳过（见
 * {@link EnabledIfDatabaseConfigured}）：
 *
 * <pre>
 * APP_DB_PASSWORD=xxx ./mvnw test -Dtest=UserRegisterApiTest
 * </pre>
 *
 * <p>
 * 类上的 {@link Transactional} 让注册出来的用户随用例一起回滚。
 *
 * @author klaus
 * @since 2026/9/28
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@RequiresRealDatabase
class UserRegisterApiTest {

    private final MockMvc mockMvc;
    private final UserMapper userMapper;
    private final PasswordHasher passwordHasher;

    UserRegisterApiTest(MockMvc mockMvc, UserMapper userMapper, PasswordHasher passwordHasher) {
        this.mockMvc = mockMvc;
        this.userMapper = userMapper;
        this.passwordHasher = passwordHasher;
    }

    @Test
    void shouldRegisterUserWithHashedPasswordAndSeniorPoints() throws Exception {
        String mobile = uniqueMobile();

        mockMvc.perform(post("/user/create")
            .contentType(MediaType.APPLICATION_JSON)
            .content(registerBody(mobile, "123456", "Passw0rd!", 31)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(jsonPath("$.message").value("ok"))
            .andExpect(jsonPath("$.data.user.mobile").value(mobile))
            .andExpect(jsonPath("$.data.user.password").doesNotExist())
            .andExpect(jsonPath("$.data.points").value(300));

        String stored = userMapper.getUserByMobile(new Mobile(mobile)).orElseThrow().getPassword();
        assertTrue(stored.startsWith("pbkdf2$"), "库里存的应该是哈希，不是明文");
        assertTrue(passwordHasher.matches("Passw0rd!", stored), "哈希应能用原密码校验通过");
    }

    @Test
    void shouldGiveJuniorPointsToUserYoungerThanThirty() throws Exception {
        mockMvc.perform(post("/user/create")
            .contentType(MediaType.APPLICATION_JSON)
            .content(registerBody(uniqueMobile(), "123456", "Passw0rd!", 29)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(jsonPath("$.data.points").value(200));
    }

    @Test
    void shouldRejectUnderageUserWithSpecErrorCode() throws Exception {
        mockMvc.perform(post("/user/create")
            .contentType(MediaType.APPLICATION_JSON)
            .content(registerBody(uniqueMobile(), "123456", "Passw0rd!", 17)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(50001))
            .andExpect(jsonPath("$.message").value("必须年满 18 周岁"));
    }

    @Test
    void shouldRejectWrongCaptchaWithUnifiedErrorBody() throws Exception {
        mockMvc.perform(post("/user/create")
            .contentType(MediaType.APPLICATION_JSON)
            .content(registerBody(uniqueMobile(), "000000", "Passw0rd!", 20)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(50007))
            .andExpect(jsonPath("$.message").value("验证码不正确"));
    }

    private static String uniqueMobile() {
        return "139" + String.format("%08d", Math.abs(System.nanoTime() % 100_000_000L));
    }

    private static String registerBody(String mobile, String captcha, String password, int age) {
        return """
            {"mobile":"%s","captcha":"%s","password":"%s","age":%d}
            """.formatted(mobile, captcha, password, age);
    }

}
