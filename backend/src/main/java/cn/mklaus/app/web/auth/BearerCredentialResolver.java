package cn.mklaus.app.web.auth;

import cn.mklaus.app.common.auth.Operator;
import cn.mklaus.app.common.auth.TokenCodec;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * 载体一：{@code Authorization: Bearer <token>}。给 App、服务间调用和开放 API 用。
 *
 * <p>
 * 优先级最高（{@code @Order(1)}）：同时带了头与 Cookie 时，以头为准。
 *
 * @author klaus
 * @since 2026/9/30
 */
@Component
@Order(1)
@AllArgsConstructor
public class BearerCredentialResolver implements OperatorCredentialResolver {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final TokenCodec tokenCodec;

    @Override
    public Optional<Operator> resolve(HttpServletRequest request) {
        String authorization = request.getHeader(AUTHORIZATION_HEADER);
        if (authorization == null || !authorization.startsWith(BEARER_PREFIX)) {
            return Optional.empty();
        }
        return tokenCodec.parseUserId(authorization.substring(BEARER_PREFIX.length()).trim())
            .map(Operator::new);
    }

}
