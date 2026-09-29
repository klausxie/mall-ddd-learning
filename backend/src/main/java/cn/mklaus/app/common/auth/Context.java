package cn.mklaus.app.common.auth;

import cn.mklaus.app.common.exception.CommonErrorCode;
import cn.mklaus.app.common.exception.ErrorCodeException;

/**
 * 当前操作人上下文。
 *
 * <p>
 * 由 web 层的 {@code OperatorContextFilter} 解析 {@code Authorization: Bearer <token>} 后写入；
 * 令牌怎么签名在 {@code infrastructure/security/HmacTokenCodec}。
 *
 * <p>
 * 未登录时直接报错，而不是返回一个 id 为 null 的空对象——后者会在下游变成难以定位的 NPE。
 *
 * @author klausxie
 * @since 2023/8/20
 */
public class Context {

    private static final ThreadLocal<Operator> CURRENT_OPERATOR = new ThreadLocal<>();

    private Context() {
    }

    /**
     * 写入当前操作人（由 web 层过滤器调用）。
     */
    public static void setOperator(Operator operator) {
        CURRENT_OPERATOR.set(operator);
    }

    /**
     * 清理当前操作人。必须在请求结束时调用，否则线程复用会把上一个请求的身份带给下一个请求。
     */
    public static void clear() {
        CURRENT_OPERATOR.remove();
    }

    /**
     * 取当前操作人，未登录抛业务错误码。
     */
    public static Operator currentOperator() {
        Operator operator = CURRENT_OPERATOR.get();
        if (operator == null) {
            throw new ErrorCodeException(CommonErrorCode.NOT_LOGGED_IN);
        }
        return operator;
    }

}
