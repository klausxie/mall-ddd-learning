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

    @Override
    public String getMessage() {
        return MessageFormat.format(errorCode.getTemplate(), args);
    }
}
