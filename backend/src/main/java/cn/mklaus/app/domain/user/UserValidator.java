package cn.mklaus.app.domain.user;

import cn.mklaus.app.common.exception.Asserts;
import cn.mklaus.app.common.exception.ErrorCodeException;
import cn.mklaus.app.domain.common.CaptchaService;
import cn.mklaus.app.domain.common.PasswordHasher;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

/**
 * 用户相关校验。所有失败都带业务错误码，不使用 Spring 的 Assert（消息无法携带错误码）。
 *
 * <p>
 * 手机号格式由值对象 {@link Mobile} 自己保证，这里只查"是否已被注册"。
 *
 * @author klausxie
 * @since 2023/8/20
 */
@Component
@AllArgsConstructor
public class UserValidator {

    /** 8-20 位，至少包含一个字母和一个数字；允许符号，但不允许空白。 */
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^(?=.*[A-Za-z])(?=.*\\d)\\S{8,20}$");

    private final UserMapper userMapper;
    private final CaptchaService captchaService;
    private final PasswordHasher passwordHasher;

    /**
     * 注册前的输入校验：验证码、手机号唯一性、密码格式。
     */
    public void assertCanRegister(String mobile, String password, String captcha) {
        Asserts.state(captchaService.isCaptchaValidate(mobile, captcha), UserErrorCode.CAPTCHA_INCORRECT);
        // 格式校验在 Mobile 的构造器里，这里只管唯一性
        assertMobileCanUse(new Mobile(mobile));
        assertPasswordValidate(password);
    }

    /**
     * 登录校验：手机号与密码必须同时匹配，通过后把实体交回调用方。
     *
     * <p>
     * 两条刻意的设计：
     *
     * <ul>
     * <li>**手机号不存在与密码错误共用一个错误码**，否则接口就成了账号枚举器；</li>
     * <li>**返回实体而不是只做断言**，免得签发令牌的调用方为了拿 id 再查一次库。</li>
     * </ul>
     */
    public User authenticate(String mobile, String rawPassword) {
        return userMapper.getUserByMobile(new Mobile(mobile))
            .filter(user -> passwordHasher.matches(rawPassword, user.getPassword()))
            .orElseThrow(() -> new ErrorCodeException(UserErrorCode.LOGIN_FAILED));
    }

    public void assertMobileCanUse(Mobile mobile) {
        Asserts.state(!userMapper.getUserByMobile(mobile).isPresent(), UserErrorCode.MOBILE_ALREADY_EXISTS);
    }

    public void assertPasswordValidate(String password) {
        Asserts.state(password != null && PASSWORD_PATTERN.matcher(password).matches(),
            UserErrorCode.PASSWORD_FORMAT_ILLEGAL);
    }

}
