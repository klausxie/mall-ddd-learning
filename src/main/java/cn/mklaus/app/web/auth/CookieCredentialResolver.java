package cn.mklaus.app.web.auth;

import cn.mklaus.app.common.auth.Operator;
import cn.mklaus.app.common.auth.TokenCodec;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Optional;

/**
 * 载体二：{@link TokenCookie} 下发的 HttpOnly Cookie。给浏览器里的后台 / 管理端用——
 * 浏览器自动携带，JS 读不到，也就不怕 XSS 偷令牌。
 *
 * <p>
 * 排在 Bearer 之后（{@code @Order(2)}）。
 *
 * @author klaus
 * @since 2026/9/30
 */
@Component
@Order(2)
@AllArgsConstructor
public class CookieCredentialResolver implements OperatorCredentialResolver {

    private final TokenCodec tokenCodec;
    private final TokenCookie tokenCookie;

    @Override
    public Optional<Operator> resolve(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return Optional.empty();
        }
        String name = tokenCookie.name();
        return Arrays.stream(cookies)
            .filter(cookie -> name.equals(cookie.getName()))
            .findFirst()
            .flatMap(cookie -> tokenCodec.parseUserId(cookie.getValue()))
            .map(Operator::new);
    }

}
