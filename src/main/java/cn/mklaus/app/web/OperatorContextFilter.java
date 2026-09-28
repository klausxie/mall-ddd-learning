package cn.mklaus.app.web;

import cn.mklaus.app.common.auth.Context;
import cn.mklaus.app.common.auth.Operator;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * 鉴权替身：真实项目在这里解析 token / session，样例里直接读 {@code X-Operator-Id} 请求头。
 *
 * <p>
 * 请求结束必须在 finally 里清理 {@link Context}，否则 Tomcat 线程复用会让身份串号。
 * 请求头缺失或非法时不写入上下文，后续需要操作人的接口会返回"未登录"。
 *
 * @author klaus
 * @since 2026/9/28
 */
@Slf4j
@Component
public class OperatorContextFilter extends OncePerRequestFilter {

    private static final String OPERATOR_ID_HEADER = "X-Operator-Id";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
        throws ServletException, IOException {
        try {
            Long operatorId = parseOperatorId(request.getHeader(OPERATOR_ID_HEADER));
            if (operatorId != null) {
                Context.setOperator(new Operator(operatorId));
            }
            chain.doFilter(request, response);
        } finally {
            Context.clear();
        }
    }

    private static Long parseOperatorId(String rawOperatorId) {
        if (rawOperatorId == null || rawOperatorId.isBlank()) {
            return null;
        }
        try {
            return Long.valueOf(rawOperatorId.trim());
        } catch (NumberFormatException e) {
            log.warn("忽略非法的 {} 请求头: {}", OPERATOR_ID_HEADER, rawOperatorId);
            return null;
        }
    }

}
