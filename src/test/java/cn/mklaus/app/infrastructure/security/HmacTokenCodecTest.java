package cn.mklaus.app.infrastructure.security;

import org.junit.jupiter.api.Test;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 令牌的签发与解析：正常往返、过期、篡改、以及"密钥没配就一律失败"。
 *
 * <p>
 * 这里刻意用 JDK 的 {@link Mac} 独立再签一遍，而不是复用实现里的方法：
 * 那样才能钉住令牌格式（改了格式这里会红），也才能造出"签名正确但内容不合规"的令牌。
 *
 * @author klaus
 * @since 2026/9/30
 */
class HmacTokenCodecTest {

    private static final String SECRET = "test-secret";
    private static final Duration TIME_TO_LIVE = Duration.ofMinutes(30);

    private final HmacTokenCodec codec = new HmacTokenCodec(SECRET, TIME_TO_LIVE);

    @Test
    void shouldParseTheUserIdItIssued() {
        assertEquals(Optional.of(42L), codec.parseUserId(codec.issue(42)));
    }

    @Test
    void shouldAcceptTokenSignedInTheDocumentedFormat() throws Exception {
        assertEquals(Optional.of(7L), codec.parseUserId(sign("7:" + expiresAtIn(60_000))));
    }

    @Test
    void shouldRejectTokenSignedWithAnotherSecret() {
        HmacTokenCodec other = new HmacTokenCodec("another-secret", TIME_TO_LIVE);

        assertTrue(codec.parseUserId(other.issue(42)).isEmpty());
    }

    @Test
    void shouldRejectTamperedClaim() {
        String token = codec.issue(42);
        String forgedClaim = base64Url(("99:" + expiresAtIn(60_000)).getBytes(StandardCharsets.UTF_8));

        assertTrue(codec.parseUserId(forgedClaim + token.substring(token.lastIndexOf('.'))).isEmpty());
    }

    @Test
    void shouldRejectExpiredToken() throws Exception {
        assertTrue(codec.parseUserId(sign("42:" + expiresAtIn(-1))).isEmpty());
        assertTrue(codec.parseUserId(new HmacTokenCodec(SECRET, Duration.ofSeconds(-1)).issue(42)).isEmpty());
    }

    @Test
    void shouldRejectMalformedTokenInsteadOfThrowing() throws Exception {
        for (String token : new String[] { null, "", "garbage", "a.b", codec.issue(42) + "x", sign("42"),
            sign("42:not-a-number"), sign("1:2:3") }) {
            assertTrue(codec.parseUserId(token).isEmpty(), () -> "非法令牌应解析为空：" + token);
        }
    }

    @Test
    void shouldFailClosedWhenSecretIsNotConfigured() {
        HmacTokenCodec unconfigured = new HmacTokenCodec("", TIME_TO_LIVE);

        assertTrue(unconfigured.parseUserId(codec.issue(42)).isEmpty());
        assertThrows(IllegalStateException.class, () -> unconfigured.issue(42));
    }

    private static String sign(String claim) throws Exception {
        String encodedClaim = base64Url(claim.getBytes(StandardCharsets.UTF_8));

        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        return encodedClaim + "." + base64Url(mac.doFinal(encodedClaim.getBytes(StandardCharsets.UTF_8)));
    }

    private static long expiresAtIn(long millis) {
        return System.currentTimeMillis() + millis;
    }

    private static String base64Url(byte[] bytes) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

}
