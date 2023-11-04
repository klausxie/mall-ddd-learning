package cn.mklaus.app.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author klausxie
 * @since 2023/11/4
 */
@Getter
@AllArgsConstructor
public enum CommonErrorCode implements ErrorCode {

    /**
     * 参数必填
     */
    PARAMETER_IS_REQUIRED(40001, "{0} is required"),

    /**
     * 实体不存在
     */
    ENTITY_NOT_FOUND(40004, "{0} not found")

    ;

    private final int code;
    private final String template;

}
