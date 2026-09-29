package cn.mklaus.app.web;

import cn.mklaus.app.web.auth.TokenCookie;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletResponse;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 令牌 Cookie 的属性：HttpOnly + SameSite=Lax 写死，Secure 可配，Max-Age 与令牌 TTL 对齐，
 * 退出时立即失效。
 *
 * <p>
 * 这几条是安全属性而不是格式细节，所以拿 {@code Set-Cookie} 原文钉住：改错一条（比如漏掉 HttpOnly、
 * 生产没开 Secure）必须让测试红。
 *
 * @author klaus
 * @since 2026/9/30
 */
class TokenCookieTest {

    private static final String NAME = "app_token";
    private static final Duration TIME_TO_LIVE = Duration.ofDays(7);

    @Test
    void shouldWriteHttpOnlyLaxCookieAlignedWithTokenTtl() {
        MockHttpServletResponse response = new MockHttpServletResponse();

        new TokenCookie(NAME, false, TIME_TO_LIVE).write(response, "a.b");

        String cookie = response.getHeader(HttpHeaders.SET_COOKIE);
        assertTrue(cookie.startsWith(NAME + "=a.b;"), cookie);
        assertTrue(cookie.contains("Path=/"), cookie);
        assertTrue(cookie.contains("Max-Age=" + TIME_TO_LIVE.toSeconds()), cookie);
        assertTrue(cookie.contains("HttpOnly"), cookie);
        assertTrue(cookie.contains("SameSite=Lax"), cookie);
        assertFalse(cookie.contains("Secure"), () -> "本地 http 调试带 Secure 会让浏览器不回传：" + cookie);
    }

    @Test
    void shouldAddSecureAttributeWhenEnabled() {
        MockHttpServletResponse response = new MockHttpServletResponse();

        new TokenCookie(NAME, true, TIME_TO_LIVE).write(response, "a.b");

        assertTrue(response.getHeader(HttpHeaders.SET_COOKIE).contains("Secure"));
    }

    @Test
    void shouldClearCookieImmediately() {
        MockHttpServletResponse response = new MockHttpServletResponse();

        new TokenCookie(NAME, false, TIME_TO_LIVE).clear(response);

        String cookie = response.getHeader(HttpHeaders.SET_COOKIE);
        assertTrue(cookie.startsWith(NAME + "="), cookie);
        assertTrue(cookie.contains("Max-Age=0"), cookie);
    }

}
