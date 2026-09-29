package cn.mklaus.app.web.auth;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * 令牌 Cookie 的名字与属性集中在这里，读（{@link CookieCredentialResolver}）和写（登录 / 退出）都用它，
 * 免得名字和属性散落成两处。
 *
 * <p>
 * 属性都是有意的：
 *
 * <ul>
 * <li>{@code HttpOnly}——JS 读不到，XSS 偷不走令牌；</li>
 * <li>{@code SameSite=Lax}——跨站 POST 不带 Cookie。本仓库"写操作一律 POST、查询用 GET"正好落在它的
 * 保护面内，所以不额外引入 CSRF 令牌；后台与其它业务**共享子域**时 Lax 挡不住，那时再加
 * double-submit CSRF token（见 README）；</li>
 * <li>{@code Secure} 由 {@code app.auth.cookie-secure} 控制（本地 http 关、生产 https 必开）；</li>
 * <li>{@code Max-Age} 与 {@code app.auth.token-ttl} 一致，浏览器不会再送一张早已过期的令牌。</li>
 * </ul>
 *
 * <p>
 * {@code SameSite=None}（跨域 SPA + 独立 API 域）需要 {@code Secure} 加 CORS 凭证配置，
 * 那种拓扑直接用 {@code Authorization} 头更省事。
 *
 * @author klaus
 * @since 2026/9/30
 */
@Component
public class TokenCookie {

    private final String name;
    private final boolean secure;
    private final Duration timeToLive;

    public TokenCookie(@Value("${app.auth.cookie-name:app_token}") String name,
        @Value("${app.auth.cookie-secure:false}") boolean secure,
        @Value("${app.auth.token-ttl:7d}") Duration timeToLive) {
        this.name = name;
        this.secure = secure;
        this.timeToLive = timeToLive;
    }

    public String name() {
        return name;
    }

    /**
     * 登录成功：把令牌写进 Cookie。
     */
    public void write(HttpServletResponse response, String token) {
        response.addHeader(HttpHeaders.SET_COOKIE, cookie(token, timeToLive).toString());
    }

    /**
     * 退出登录：让浏览器立刻丢掉 Cookie。
     *
     * <p>
     * 自签令牌**无法在服务端吊销**，所以这里只是"客户端不再带它"；需要"改密即失效 / 强制下线"时，
     * 必须换成服务端会话（见 README「鉴权」）。
     */
    public void clear(HttpServletResponse response) {
        response.addHeader(HttpHeaders.SET_COOKIE, cookie("", Duration.ZERO).toString());
    }

    private ResponseCookie cookie(String value, Duration maxAge) {
        return ResponseCookie.from(name, value)
            .httpOnly(true)
            .sameSite("Lax")
            .secure(secure)
            .path("/")
            .maxAge(maxAge)
            .build();
    }

}
