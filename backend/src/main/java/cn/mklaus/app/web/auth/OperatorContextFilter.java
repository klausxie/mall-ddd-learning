package cn.mklaus.app.web.auth;

import cn.mklaus.app.common.auth.Context;
import cn.mklaus.app.common.auth.Operator;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * 装配当前操作人：依次问各个 {@link OperatorCredentialResolver}，第一个解析出身份的就生效，写进
 * {@link Context}。凭证怎么传（Bearer 头 / Cookie）与令牌怎么签名都被这一步挡在外面，
 * 用例里只认 {@code Context.currentOperator()}。
 *
 * <p>
 * 解析不出身份也不报错，由后续需要身份的接口报"未登录"（40005）——与全局"HTTP 状态恒为 200、
 * 靠 body.code 区分"的约定一致，也为注册 / 登录这类公开接口留了路。
 *
 * <p>
 * 请求结束必须在 finally 里清理 {@link Context}，否则 Tomcat 线程复用会让身份串号。
 *
 * @author klaus
 * @since 2026/9/28
 */
@Component
@AllArgsConstructor
public class OperatorContextFilter extends OncePerRequestFilter {

    private final List<OperatorCredentialResolver> credentialResolvers;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
        throws ServletException, IOException {
        try {
            resolveOperator(request).ifPresent(Context::setOperator);
            chain.doFilter(request, response);
        } finally {
            Context.clear();
        }
    }

    private Optional<Operator> resolveOperator(HttpServletRequest request) {
        return credentialResolvers.stream()
            .map(resolver -> resolver.resolve(request))
            .flatMap(Optional::stream)
            .findFirst();
    }

}
