package cn.mklaus.app.web;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestConstructor;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * OpenAPI 描述可用性：不需要数据库，但需要上下文（所以排在真库测试之外也能跑）。
 *
 * @author klaus
 * @since 2026/9/28
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class OpenApiDocsTest {

    private final MockMvc mockMvc;

    OpenApiDocsTest(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }

    @Test
    void shouldExposeOpenApiDescriptionForAllControllers() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.openapi").exists())
            .andExpect(jsonPath("$.paths['/user/create']").exists())
            .andExpect(jsonPath("$.paths['/address/page']").exists())
            .andExpect(jsonPath("$.paths['/product/page']").exists());
    }

}
