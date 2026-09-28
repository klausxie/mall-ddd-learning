package cn.mklaus.app.common.exception;

import lombok.Getter;

import java.text.MessageFormat;

/**
 * 一次约束违规：错误码 + 信息模板参数。
 *
 * <p>
 * 与 {@link ErrorCodeException} 的区别是它是"值"而不是"异常"，
 * 因此可以在不抛出的前提下被收集、比较与断言。
 *
 * @author klaus
 * @since 2026/9/28
 */
@Getter
public class Violation {

    private final ErrorCode errorCode;

    private final Object[] args;

    private Violation(ErrorCode errorCode, Object[] args) {
        this.errorCode = errorCode;
        this.args = args;
    }

    /**
     * 创建违规原因，{@code args} 用于填充错误码的信息模板。
     */
    public static Violation of(ErrorCode errorCode, Object... args) {
        return new Violation(errorCode, args);
    }

    /**
     * 渲染后的错误信息，规则与 {@link ErrorCodeException} 保持一致。
     */
    public String getMessage() {
        return MessageFormat.format(errorCode.getTemplate(), args);
    }

}
