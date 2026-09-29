package cn.mklaus.app.common.exception;

import lombok.NoArgsConstructor;

/**
 * 基础异常类
 *
 * @author klausxie
 * @since 2023/11/4
 */
@NoArgsConstructor
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }

}
