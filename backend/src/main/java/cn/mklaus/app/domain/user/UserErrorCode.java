package cn.mklaus.app.domain.user;

import cn.mklaus.app.common.exception.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author klaus
 * @since 2026/9/28
 */
@Getter
@AllArgsConstructor
public enum UserErrorCode implements ErrorCode {

    /**
     * 用户未成年
     */
    USER_MUST_BE_ADULT(50001, "必须年满 {0} 周岁"),

    /**
     * 地址不存在
     */
    ADDRESS_NOT_EXISTS(50002, "地址不存在"),

    /**
     * 用户不存在
     */
    USER_NOT_EXISTS(50003, "用户不存在"),

    /**
     * 手机号格式不正确
     */
    MOBILE_FORMAT_ILLEGAL(50004, "手机号码格式不正确"),

    /**
     * 手机号已存在
     */
    MOBILE_ALREADY_EXISTS(50005, "手机号码已存在"),

    /**
     * 密码格式不正确
     */
    PASSWORD_FORMAT_ILLEGAL(50006, "密码格式不正确"),

    /**
     * 验证码不正确
     */
    CAPTCHA_INCORRECT(50007, "验证码不正确"),

    /**
     * 没有权限
     */
    NO_PERMISSION(50008, "没有权限"),

    /**
     * 收件人不能为空
     */
    RECIPIENT_IS_REQUIRED(50009, "收件人不能为空"),

    /**
     * 登录失败（手机号不存在或密码不正确，刻意不区分，避免账号枚举）
     */
    LOGIN_FAILED(50010, "手机号或密码不正确")

    ;

    private final int code;
    private final String template;

}
