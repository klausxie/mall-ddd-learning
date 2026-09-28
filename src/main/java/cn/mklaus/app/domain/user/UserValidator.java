package cn.mklaus.app.domain.user;

import cn.mklaus.app.common.exception.Asserts;
import cn.mklaus.app.domain.common.CaptchaService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

/**
 * 用户相关校验。所有失败都带业务错误码，不使用 Spring 的 Assert（消息无法携带错误码）。
 *
 * @author klausxie
 * @since 2023/8/20
 */
@Component
@AllArgsConstructor
public class UserValidator {

    private static final Pattern MOBILE_PATTERN = Pattern.compile("1[3-9]\\d{9}");

    /** 8-20 位，至少包含一个字母和一个数字；允许符号，但不允许空白。 */
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^(?=.*[A-Za-z])(?=.*\\d)\\S{8,20}$");

    private final UserMapper userMapper;
    private final CaptchaService captchaService;

    /**
     * 注册前的输入校验：验证码、手机号格式与唯一性、密码格式。
     */
    public void assertCanRegister(String mobile, String password, String captcha) {
        Asserts.state(captchaService.isCaptchaValidate(mobile, captcha), UserErrorCode.CAPTCHA_INCORRECT);
        assertMobileCanUse(mobile);
        assertPasswordValidate(password);
    }

    public void assertMobileCanUse(String mobile) {
        Asserts.state(mobile != null && MOBILE_PATTERN.matcher(mobile).matches(), UserErrorCode.MOBILE_FORMAT_ILLEGAL);
        Asserts.state(!userMapper.getUserByMobile(mobile).isPresent(), UserErrorCode.MOBILE_ALREADY_EXISTS);
    }

    public void assertPasswordValidate(String password) {
        Asserts.state(password != null && PASSWORD_PATTERN.matcher(password).matches(),
            UserErrorCode.PASSWORD_FORMAT_ILLEGAL);
    }

}
