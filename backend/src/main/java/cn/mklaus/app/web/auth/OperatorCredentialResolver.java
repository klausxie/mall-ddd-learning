package cn.mklaus.app.web.auth;

import cn.mklaus.app.common.auth.Operator;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Optional;

/**
 * 从请求里取出凭证并解析成当前操作人。
 *
 * <p>
 * "凭证从哪来"是协议层的事，所以接口和实现都在 web（{@code common} 不必为此依赖 servlet）：
 * 现在有 {@link BearerCredentialResolver}（{@code Authorization: Bearer}）与
 * {@link CookieCredentialResolver}（HttpOnly Cookie）两种载体。要接服务端会话时，再加一个实现即可，
 * {@code domain} / {@code application} 一行都不用改。
 *
 * <p>
 * 多个实现由 {@link OperatorContextFilter} 按 {@code @Order} 依次尝试，**第一个解析成功的生效**——
 * 所以"带了非法 Bearer 头、但 Cookie 有效"的浏览器仍能正常访问。
 *
 * @author klaus
 * @since 2026/9/30
 */
public interface OperatorCredentialResolver {

    /**
     * 解析不出身份（没带凭证、凭证非法或已过期）时返回空。
     */
    Optional<Operator> resolve(HttpServletRequest request);

}
