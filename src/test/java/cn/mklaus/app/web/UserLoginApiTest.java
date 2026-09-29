package cn.mklaus.app.web;

import cn.mklaus.app.common.auth.TokenCodec;
import cn.mklaus.app.domain.common.PasswordHasher;
import cn.mklaus.app.domain.user.Mobile;
import cn.mklaus.app.domain.user.User;
import cn.mklaus.app.domain.user.UserMapper;
import cn.mklaus.app.support.RequiresRealDatabase;
import cn.mklaus.app.web.auth.TokenCookie;
import com.jayway.jsonpath.JsonPath;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestConstructor;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 登录端到端：凭据校验 → 签发令牌 → 用令牌（Bearer 头或 Cookie）访问受保护接口；
 * 以及未登录 / 令牌被篡改 / 两种载体同时存在时的表现。
 *
 * <p>
 * 需要真实数据库，数据源配了密码就跑，没配则跳过（见 {@link RequiresRealDatabase}）：
 *
 * <pre>
 * MALL_DB_PASSWORD=xxx ./mvnw test -Dtest=UserLoginApiTest
 * </pre>
 *
 * <p>
 * 类上的 {@link Transactional} 让建出来的用户随用例一起回滚。
 *
 * @author klaus
 * @since 2026/9/30
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@RequiresRealDatabase
class UserLoginApiTest {

    private static final String RAW_PASSWORD = "Passw0rd!";

    private final MockMvc mockMvc;
    private final UserMapper userMapper;
    private final PasswordHasher passwordHasher;
    private final TokenCodec tokenCodec;
    private final TokenCookie tokenCookie;

    private String mobile;
    private String unknownMobile;

    UserLoginApiTest(MockMvc mockMvc, UserMapper userMapper, PasswordHasher passwordHasher, TokenCodec tokenCodec,
        TokenCookie tokenCookie) {
        this.mockMvc = mockMvc;
        this.userMapper = userMapper;
        this.passwordHasher = passwordHasher;
        this.tokenCodec = tokenCodec;
        this.tokenCookie = tokenCookie;
    }

    @BeforeEach
    void prepareUser() {
        mobile = uniqueMobile("139");
        unknownMobile = uniqueMobile("138");

        User user = new User();
        user.setMobile(new Mobile(mobile));
        user.setPassword(passwordHasher.encode(RAW_PASSWORD));
        user.setAge(30);
        userMapper.saveUser(user);
    }

    @Test
    void shouldLoginWithTokenAndReachProtectedEndpoint() throws Exception {
        String body = mockMvc.perform(post("/user/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(loginBody(mobile, RAW_PASSWORD)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(jsonPath("$.data.user.mobile").value(mobile))
            .andExpect(jsonPath("$.data.user.password").doesNotExist())
            .andExpect(jsonPath("$.data.token").isNotEmpty())
            .andReturn()
            .getResponse()
            .getContentAsString();

        mockMvc.perform(get("/user/get").header("Authorization", "Bearer " + JsonPath.read(body, "$.data.token")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(jsonPath("$.data.mobile").value(mobile));
    }

    @Test
    void shouldReachProtectedEndpointWithCookieFromLogin() throws Exception {
        MvcResult result = mockMvc.perform(post("/user/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(loginBody(mobile, RAW_PASSWORD)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("HttpOnly")))
            .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("SameSite=Lax")))
            .andReturn();

        String token = JsonPath.read(result.getResponse().getContentAsString(), "$.data.token");

        // 浏览器就是这样访问的：JS 拿不到令牌，只靠浏览器自动带上 Cookie
        mockMvc.perform(get("/user/get").cookie(new Cookie(tokenCookie.name(), token)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(jsonPath("$.data.mobile").value(mobile));
    }

    /**
     * 两种载体同时存在时以 Bearer 头为准（{@code @Order(1)}），避免"到底用哪个身份"含糊。
     */
    @Test
    void shouldPreferAuthorizationHeaderOverCookie() throws Exception {
        String otherMobile = uniqueMobile("137");
        User other = new User();
        other.setMobile(new Mobile(otherMobile));
        other.setPassword("pbkdf2$600000$c2FsdA==$aGFzaA==");
        other.setAge(30);
        userMapper.saveUser(other);

        mockMvc.perform(get("/user/get")
            .cookie(new Cookie(tokenCookie.name(), login()))
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenCodec.issue(other.getId())))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(jsonPath("$.data.mobile").value(otherMobile));
    }

    @Test
    void shouldClearTokenCookieOnLogout() throws Exception {
        mockMvc.perform(post("/user/logout"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("Max-Age=0")));
    }

    @Test
    void shouldRejectWrongPasswordWithoutSayingWhichPartIsWrong() throws Exception {
        mockMvc.perform(post("/user/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(loginBody(mobile, "Wrong0000")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(50010))
            .andExpect(jsonPath("$.message").value("手机号或密码不正确"));
    }

    @Test
    void shouldRejectUnknownMobileWithTheSameErrorCode() throws Exception {
        mockMvc.perform(post("/user/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(loginBody(unknownMobile, RAW_PASSWORD)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(50010))
            .andExpect(jsonPath("$.message").value("手机号或密码不正确"));
    }

    @Test
    void shouldReportNotLoggedInWhenTokenMissing() throws Exception {
        mockMvc.perform(get("/user/get"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(40005))
            .andExpect(jsonPath("$.message").value("未登录"));
    }

    @Test
    void shouldReportNotLoggedInWhenTokenIsTampered() throws Exception {
        mockMvc.perform(get("/user/get").header("Authorization", "Bearer " + login() + "0"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(40005))
            .andExpect(jsonPath("$.message").value("未登录"));
    }

    /**
     * 登录一次并取出令牌。
     */
    private String login() throws Exception {
        String body = mockMvc.perform(post("/user/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(loginBody(mobile, RAW_PASSWORD)))
            .andExpect(jsonPath("$.code").value(0))
            .andReturn()
            .getResponse()
            .getContentAsString();
        return JsonPath.read(body, "$.data.token");
    }

    private static String loginBody(String mobile, String password) {
        return """
            {"mobile":"%s","password":"%s"}
            """.formatted(mobile, password);
    }

    private static String uniqueMobile(String prefix) {
        return prefix + String.format("%08d", Math.abs(System.nanoTime() % 100_000_000L));
    }

}
