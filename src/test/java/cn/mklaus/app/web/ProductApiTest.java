package cn.mklaus.app.web;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
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
 * 商品接口端到端：新增 → 分页过滤 → 上架 → 下架 → 删除规则 → 参数校验。
 *
 * <p>
 * 需要真实数据库，默认跳过：
 *
 * <pre>
 * MALL_DB_PASSWORD=xxx ./mvnw test -Dtest=ProductApiTest
 * </pre>
 *
 * @author klaus
 * @since 2026/9/28
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@EnabledIfEnvironmentVariable(named = "MALL_DB_PASSWORD", matches = ".+")
class ProductApiTest {

    private final MockMvc mockMvc;

    ProductApiTest(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }

    @Test
    void shouldCreateFilterOnSaleOffSaleAndKeepOnSaleProductUndeletable() throws Exception {
        String keyword = "api" + System.nanoTime();
        long productId = createProduct(keyword);

        mockMvc.perform(get("/product/page").param("keyword", keyword))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(jsonPath("$.data.curPage").value(1))
            .andExpect(jsonPath("$.data.total").value(1))
            .andExpect(jsonPath("$.data.records[0].name").value(keyword))
            .andExpect(jsonPath("$.data.records[0].status").value("PENDING"));

        mockMvc.perform(post("/product/onSale")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"productId\":" + productId + "}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0));

        mockMvc.perform(get("/product/page").param("keyword", keyword).param("status", "ON_SALE"))
            .andExpect(jsonPath("$.data.total").value(1))
            .andExpect(jsonPath("$.data.records[0].status").value("ON_SALE"));

        mockMvc.perform(post("/product/offSale")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"productId\":" + productId + "}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0));

        mockMvc.perform(get("/product/page").param("keyword", keyword))
            .andExpect(jsonPath("$.data.records[0].status").value("OFF_SALE"));

        // 上架过的商品不允许删除
        mockMvc.perform(post("/product/remove")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"productId\":" + productId + "}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(60004))
            .andExpect(jsonPath("$.message").value("已上架过的商品不能删除"));
    }

    @Test
    void shouldRemoveProductThatNeverWentOnSale() throws Exception {
        String keyword = "api" + System.nanoTime();
        long productId = createProduct(keyword);

        mockMvc.perform(post("/product/remove")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"productId\":" + productId + "}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0));

        mockMvc.perform(get("/product/page").param("keyword", keyword))
            .andExpect(jsonPath("$.data.total").value(0));
    }

    @Test
    void shouldReportPriceRuleFromDomain() throws Exception {
        mockMvc.perform(post("/product/create")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"name\":\"api" + System.nanoTime() + "\",\"price\":0,\"inventory\":1}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(60005))
            .andExpect(jsonPath("$.message").value("价格不能小于等于0"));
    }

    @Test
    void shouldReportMissingFieldFromBeanValidation() throws Exception {
        mockMvc.perform(post("/product/create")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"name\":\"  \",\"price\":100,\"inventory\":1}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(40002))
            .andExpect(jsonPath("$.message").value("商品名称不能为空"));
    }

    private long createProduct(String name) throws Exception {
        String created = mockMvc.perform(post("/product/create")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"name\":\"" + name + "\",\"description\":\"api\",\"content\":\"api\","
                + "\"cover\":\"http://example.com/cover.png\",\"price\":1999,\"inventory\":3}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(jsonPath("$.data.name").value(name))
            .andReturn()
            .getResponse()
            .getContentAsString();
        return ((Number) JsonPath.read(created, "$.data.id")).longValue();
    }

}
