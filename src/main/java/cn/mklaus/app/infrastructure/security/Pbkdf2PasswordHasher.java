package cn.mklaus.app.infrastructure.security;

import cn.mklaus.app.domain.common.PasswordHasher;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

/**
 * 基于 JDK 自带 PBKDF2-HMAC-SHA256 的密码哈希实现，不引入第三方加密库。
 *
 * <p>
 * 存储格式：{@code pbkdf2$迭代次数$Base64(salt)$Base64(hash)}。
 * 参数随哈希一起存，所以以后提高迭代次数不会让历史密码失效。
 * 迭代次数取 OWASP 对 PBKDF2-HMAC-SHA256 的推荐值 600000。
 *
 * <p>
 * 比较用 {@link MessageDigest#isEqual} 做常量时间比较，避免计时侧信道。
 *
 * @author klaus
 * @since 2026/9/28
 */
@Component
public class Pbkdf2PasswordHasher implements PasswordHasher {

    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final String PREFIX = "pbkdf2";
    private static final int ITERATIONS = 600_000;
    private static final int SALT_BYTES = 16;
    private static final int KEY_BITS = 256;

    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    public String encode(String rawPassword) {
        byte[] salt = new byte[SALT_BYTES];
        secureRandom.nextBytes(salt);

        byte[] hash = pbkdf2(rawPassword, salt, ITERATIONS);
        Base64.Encoder encoder = Base64.getEncoder();
        return String.join("$", PREFIX, String.valueOf(ITERATIONS),
            encoder.encodeToString(salt), encoder.encodeToString(hash));
    }

    @Override
    public boolean matches(String rawPassword, String encodedPassword) {
        if (rawPassword == null || encodedPassword == null) {
            return false;
        }

        String[] parts = encodedPassword.split("\\$");
        if (parts.length != 4 || !PREFIX.equals(parts[0])) {
            return false;
        }

        try {
            int iterations = Integer.parseInt(parts[1]);
            byte[] salt = Base64.getDecoder().decode(parts[2]);
            byte[] expected = Base64.getDecoder().decode(parts[3]);
            return MessageDigest.isEqual(expected, pbkdf2(rawPassword, salt, iterations));
        } catch (IllegalArgumentException e) {
            // 库里存了非法格式的哈希：当作校验失败，不要抛出去
            return false;
        }
    }

    private static byte[] pbkdf2(String rawPassword, byte[] salt, int iterations) {
        PBEKeySpec spec = new PBEKeySpec(rawPassword.toCharArray(), salt, iterations, KEY_BITS);
        try {
            return SecretKeyFactory.getInstance(ALGORITHM).generateSecret(spec).getEncoded();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new IllegalStateException("密码哈希失败", e);
        } finally {
            spec.clearPassword();
        }
    }

}
