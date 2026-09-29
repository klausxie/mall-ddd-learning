package cn.mklaus.app.domain.common;

/**
 * 密码哈希能力。
 *
 * <p>
 * 接口放 domain、实现放 infrastructure：这样 application / domain 都不感知具体算法，
 * 以后从 PBKDF2 换成 BCrypt / Argon2 只需要替换实现类。
 *
 * @author klaus
 * @since 2026/9/28
 */
public interface PasswordHasher {

    /**
     * 生成可入库的密码哈希。
     */
    String encode(String rawPassword);

    /**
     * 校验明文密码与库存哈希是否匹配。
     */
    boolean matches(String rawPassword, String encodedPassword);

}
