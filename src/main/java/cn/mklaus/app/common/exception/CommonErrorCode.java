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
     * 参数不合法
     */
    PARAMETER_ILLEGAL(40002, "{0}"),

    /**
     * 业务状态不允许（Assert.state）
     */
    STATE_ILLEGAL(40003, "{0}"),

    /**
     * 未登录（缺少操作人身份）
     */
    NOT_LOGGED_IN(40005, "未登录"),

    /**
     * curPage 非法
     */
    CUR_PAGE_ILLEGAL(40006, "curPage必须大于0"),

    /**
     * pageSize 非法
     */
    PAGE_SIZE_ILLEGAL(40007, "pageSize必须大于0"),

    /**
     * 系统异常（兜底，不向客户端暴露内部细节）
     */
    SYSTEM_ERROR(90000, "系统繁忙，请稍后重试")

    ;

    private final int code;
    private final String template;

}
