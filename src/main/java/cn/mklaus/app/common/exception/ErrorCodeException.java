package cn.mklaus.app.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.text.MessageFormat;

/**
 * 错误码异常类
 *
 * @author klausxie
 * @since 2023/11/4
 */
@Getter
@AllArgsConstructor
public class ErrorCodeException extends MallException {

    private final ErrorCode errorCode;
    private final Object[] args;

    /**
     * 错误码的信息模板不需要填参数时用这个构造器。
     */
    public ErrorCodeException(ErrorCode errorCode) {
        this(errorCode, new Object[0]);
    }

    @Override
    public String getMessage() {
        return MessageFormat.format(errorCode.getTemplate(), args);
    }
}
