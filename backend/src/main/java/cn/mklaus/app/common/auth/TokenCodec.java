package cn.mklaus.app.common.auth;

import java.util.Optional;

/**
 * 登录令牌的签发与解析。
 *
 * <p>
 * 放在 {@code common} 而不是 {@code domain}：解析令牌是 web 层过滤器为请求建立身份的最后一步，
 * 而分层规则禁止 web 依赖 domain（见 {@code ArchitectureTest}）；它本身也不含任何业务含义，
 * 只把"用户 ID"和"一串字符"互相转换。
 *
 * <p>
 * 具体怎么签名（HMAC、JWT、Redis 里查）是实现细节，见 {@code infrastructure/security}；
 * 接口只表达两件事：给用户 ID 签发令牌、把令牌换回用户 ID。
 *
 * @author klaus
 * @since 2026/9/30
 */
public interface TokenCodec {

    /**
     * 为指定用户签发令牌。
     */
    String issue(long userId);

    /**
     * 解析令牌并取出用户 ID。
     *
     * <p>
     * 签名不合法、格式不对、已过期一律返回空——调用方不需要区分失败原因，
     * 拿不到身份就是未登录。
     */
    Optional<Long> parseUserId(String token);

}
