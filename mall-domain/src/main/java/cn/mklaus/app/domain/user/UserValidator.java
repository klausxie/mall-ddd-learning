package cn.mklaus.app.domain.user;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.regex.Pattern;

/**
 * @author klausxie
 * @since 2023/8/20
 */
@Component
@AllArgsConstructor
public class UserValidator {

    private final static Pattern MOBILE_PATTERN = Pattern.compile("1[3-9]\\d{9}");
    private final static Pattern PASSWORD_PATTERN = Pattern.compile("1[3-9]\\d{9}");

    private final UserRepository userRepository;

    public void assertMobileCanUse(String mobile) {
        Assert.state(MOBILE_PATTERN.matcher(mobile).matches(), "手机号码格式不正确");
        Assert.state(!userRepository.getUserByMobile(mobile).isPresent(), "手机号码已存在");
    }

    public void assertPasswordValidate(String password) {
        Assert.state(PASSWORD_PATTERN.matcher(password).matches(), "密码格式不正确");
    }

}
