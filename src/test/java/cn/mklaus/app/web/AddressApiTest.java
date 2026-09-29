package cn.mklaus.app.web;

import cn.mklaus.app.common.auth.TokenCodec;
import cn.mklaus.app.domain.user.Mobile;
import cn.mklaus.app.domain.user.User;
import cn.mklaus.app.domain.user.UserMapper;
import cn.mklaus.app.support.RequiresRealDatabase;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestConstructor;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 地址接口端到端：登录令牌 → 解析出操作人 → 归属校验 → 增删改查 + 分页。
 *
 * <p>
 * 需要真实数据库，数据源配了密码就跑，没配则跳过（见 {@link RequiresRealDatabase}）：
 *
 * <pre>
 * MALL_DB_PASSWORD=xxx ./mvnw test -Dtest=AddressApiTest
 * </pre>
 *
 * @author klaus
 * @since 2026/9/28
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@RequiresRealDatabase
class AddressApiTest {

    private static final String AUTHORIZATION_HEADER = "Authorization";

    private final MockMvc mockMvc;
    private final UserMapper userMapper;
    private final TokenCodec tokenCodec;

    private String authorization;

    AddressApiTest(MockMvc mockMvc, UserMapper userMapper, TokenCodec tokenCodec) {
        this.mockMvc = mockMvc;
        this.userMapper = userMapper;
        this.tokenCodec = tokenCodec;
    }

    @BeforeEach
    void prepareOperator() {
        User user = new User();
        user.setMobile(new Mobile("139" + String.format("%08d", Math.abs(System.nanoTime() % 100_000_000L))));
        user.setPassword("pbkdf2$600000$c2FsdA==$aGFzaA==");
        user.setAge(30);
        userMapper.saveUser(user);
        authorization = "Bearer " + tokenCodec.issue(user.getId());
    }

    @Test
    void shouldCreatePageUpdateAndRemoveAddress() throws Exception {
        String created = mockMvc.perform(post("/address/create")
            .header(AUTHORIZATION_HEADER, authorization)
            .contentType(MediaType.APPLICATION_JSON)
            .content(addressBody(null, "收件人", "科技园 1 号")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(jsonPath("$.data.recipient").value("收件人"))
            .andReturn()
            .getResponse()
            .getContentAsString();
        long addressId = ((Number) JsonPath.read(created, "$.data.id")).longValue();

        mockMvc.perform(get("/address/page").header(AUTHORIZATION_HEADER, authorization))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(jsonPath("$.data.curPage").value(1))
            .andExpect(jsonPath("$.data.pageSize").value(10))
            .andExpect(jsonPath("$.data.total").value(1))
            .andExpect(jsonPath("$.data.records[0].recipient").value("收件人"));

        mockMvc.perform(post("/address/update")
            .header(AUTHORIZATION_HEADER, authorization)
            .contentType(MediaType.APPLICATION_JSON)
            .content(addressBody(addressId, "新收件人", "科技园 2 号")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0));

        mockMvc.perform(get("/address/page").header(AUTHORIZATION_HEADER, authorization))
            .andExpect(jsonPath("$.data.records[0].recipient").value("新收件人"))
            .andExpect(jsonPath("$.data.records[0].detail").value("科技园 2 号"));

        mockMvc.perform(post("/address/remove")
            .header(AUTHORIZATION_HEADER, authorization)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"addressId\":" + addressId + "}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0));

        mockMvc.perform(get("/address/page").header(AUTHORIZATION_HEADER, authorization))
            .andExpect(jsonPath("$.data.total").value(0));
    }

    @Test
    void shouldReportNotLoggedInWhenTokenMissing() throws Exception {
        mockMvc.perform(get("/address/page"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(40005))
            .andExpect(jsonPath("$.message").value("未登录"));
    }

    @Test
    void shouldReportAddressNotExistsForUnknownAddress() throws Exception {
        mockMvc.perform(post("/address/remove")
            .header(AUTHORIZATION_HEADER, authorization)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"addressId\":-1}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(50002))
            .andExpect(jsonPath("$.message").value("地址不存在"));
    }

    private static String addressBody(Long addressId, String recipient, String detail) {
        String idField = addressId == null ? "" : "\"addressId\":" + addressId + ",";
        return """
            {%s"recipient":"%s","phone":"13900000000","province":"广东省","city":"深圳市","district":"南山区","detail":"%s"}
            """.formatted(idField, recipient, detail);
    }

}
