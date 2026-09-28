package cn.mklaus.app.common.exception;

import lombok.NoArgsConstructor;

/**
 * 基础异常类
 *
 * @author klausxie
 * @since 2023/11/4
 */
@NoArgsConstructor
public class BaseException extends RuntimeException {

    public BaseException(String message) {
        super(message);
    }

    public BaseException(String message, Throwable cause) {
        super(message, cause);
    }

}
