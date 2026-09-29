package cn.mklaus.app.infrastructure.security;

import cn.mklaus.app.common.auth.TokenCodec;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.Base64;
import java.util.Optional;

/**
 * 自签令牌实现：用 JDK 自带的 HMAC-SHA256 签名，不引入第三方 JWT 库（与 {@code Pbkdf2PasswordHasher} 同一取舍）。
 *
 * <p>
 * 令牌格式：{@code Base64URL(userId:过期时间毫秒)} + {@code "."} + {@code Base64URL(HMAC-SHA256(前一段))}。
 * 只在本服务内签发与校验，所以不需要 JWT 的 header / alg 协商——那恰好是 JWT 最容易出错的地方
 * （algorithm confusion、alg=none），两段格式反而更少、更容易看懂。
 *
 * <p>
 * 密钥来自 {@code app.auth.token-secret}：**未配置时一律失败**（签发抛异常、解析返回空），
 * 宁可把登录拦住，也不要用可预测的密钥放行；真实项目必须从环境变量 / 密钥管理注入。
 *
 * @author klaus
 * @since 2026/9/30
 */
@Slf4j
@Component
public class HmacTokenCodec implements TokenCodec {

    private static final String ALGORITHM = "HmacSHA256";
    private static final String SEPARATOR = ".";
    private static final String CLAIM_SEPARATOR = ":";

    /**
     * 未配置密钥时为 {@code null}：所有出口都失败（fail closed）。
     */
    private final SecretKeySpec key;
    private final Duration timeToLive;

    public HmacTokenCodec(@Value("${app.auth.token-secret:}") String secret,
        @Value("${app.auth.token-ttl:7d}") Duration timeToLive) {
        this.timeToLive = timeToLive;
        this.key = secret.isBlank() ? null : new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), ALGORITHM);
        if (this.key == null) {
            log.warn("app.auth.token-secret 未配置：登录会失败，请注入签名密钥（见 TEMPLATE.md 的样例替身清单）");
        }
    }

    @Override
    public String issue(long userId) {
        if (key == null) {
            throw new IllegalStateException("app.auth.token-secret 未配置，无法签发令牌");
        }
        String claim = userId + CLAIM_SEPARATOR + (System.currentTimeMillis() + timeToLive.toMillis());
        String encodedClaim = base64Url(claim.getBytes(StandardCharsets.UTF_8));
        return encodedClaim + SEPARATOR + base64Url(sign(encodedClaim));
    }

    @Override
    public Optional<Long> parseUserId(String token) {
        if (key == null || token == null) {
            return Optional.empty();
        }

        int split = token.lastIndexOf(SEPARATOR);
        if (split <= 0 || split == token.length() - 1) {
            return Optional.empty();
        }

        String encodedClaim = token.substring(0, split);
        try {
            byte[] signature = Base64.getUrlDecoder().decode(token.substring(split + 1));
            // 常量时间比较，避免用响应耗时逐字节猜签名
            if (!MessageDigest.isEqual(sign(encodedClaim), signature)) {
                return Optional.empty();
            }

            String[] claim = new String(Base64.getUrlDecoder().decode(encodedClaim), StandardCharsets.UTF_8)
                .split(CLAIM_SEPARATOR, -1);
            if (claim.length != 2) {
                return Optional.empty();
            }
            long expiresAt = Long.parseLong(claim[1]);
            return expiresAt > System.currentTimeMillis() ? Optional.of(Long.parseLong(claim[0])) : Optional.empty();
        } catch (RuntimeException e) {
            // 格式不对 / Base64 坏了 / 不是数字，都等价于"令牌无效"，不该变成 500
            log.debug("令牌解析失败，按未登录处理", e);
            return Optional.empty();
        }
    }

    private byte[] sign(String encodedClaim) {
        try {
            Mac mac = Mac.getInstance(ALGORITHM);
            mac.init(key);
            return mac.doFinal(encodedClaim.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new IllegalStateException("令牌签名失败", e);
        }
    }

    private static String base64Url(byte[] bytes) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

}
