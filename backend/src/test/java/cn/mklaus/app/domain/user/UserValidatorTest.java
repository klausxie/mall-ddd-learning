package cn.mklaus.app.domain.user;

import cn.mklaus.app.common.exception.ErrorCodeException;
import cn.mklaus.app.domain.common.CaptchaService;
import cn.mklaus.app.domain.common.PasswordHasher;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author klaus
 * @since 2026/9/28
 */
class UserValidatorTest {

    private static final String MOBILE = "13900000000";
    private static final String PASSWORD = "Passw0rd!";
    private static final String CAPTCHA = "123456";

    private final UserMapper userMapper = mock(UserMapper.class);
    private final CaptchaService captchaService = mock(CaptchaService.class);
    private final PasswordHasher passwordHasher = mock(PasswordHasher.class);
    private final UserValidator validator = new UserValidator(userMapper, captchaService, passwordHasher);

    @Test
    void shouldRejectIllegalMobileFormatWhenBuildingValueObject() {
        assertCode(UserErrorCode.MOBILE_FORMAT_ILLEGAL, () -> new Mobile("12345"));
        assertCode(UserErrorCode.MOBILE_FORMAT_ILLEGAL, () -> new Mobile(null));
        assertCode(UserErrorCode.MOBILE_FORMAT_ILLEGAL, () -> new Mobile("23900000000"));
    }

    @Test
    void shouldRejectMobileAlreadyRegistered() {
        Mobile mobile = new Mobile(MOBILE);
        when(userMapper.getUserByMobile(mobile)).thenReturn(Optional.of(new User()));

        assertCode(UserErrorCode.MOBILE_ALREADY_EXISTS, () -> validator.assertMobileCanUse(mobile));
    }

    @Test
    void shouldRejectIllegalPassword() {
        assertCode(UserErrorCode.PASSWORD_FORMAT_ILLEGAL, () -> validator.assertPasswordValidate("short1"));
        assertCode(UserErrorCode.PASSWORD_FORMAT_ILLEGAL, () -> validator.assertPasswordValidate("allletters"));
        assertCode(UserErrorCode.PASSWORD_FORMAT_ILLEGAL, () -> validator.assertPasswordValidate("12345678"));
        assertCode(UserErrorCode.PASSWORD_FORMAT_ILLEGAL, () -> validator.assertPasswordValidate(null));
    }

    @Test
    void shouldAcceptPasswordWithSymbols() {
        assertDoesNotThrow(() -> validator.assertPasswordValidate("Passw0rd!"));
        assertDoesNotThrow(() -> validator.assertPasswordValidate("Passw0rd"));
    }

    @Test
    void shouldRejectWrongCaptchaBeforeAnythingElse() {
        when(captchaService.isCaptchaValidate(MOBILE, "000000")).thenReturn(false);

        assertCode(UserErrorCode.CAPTCHA_INCORRECT,
            () -> validator.assertCanRegister(MOBILE, PASSWORD, "000000"));
    }

    @Test
    void shouldPassRegisterChecksWhenEverythingIsFine() {
        when(captchaService.isCaptchaValidate(MOBILE, CAPTCHA)).thenReturn(true);
        when(userMapper.getUserByMobile(new Mobile(MOBILE))).thenReturn(Optional.empty());

        assertDoesNotThrow(() -> validator.assertCanRegister(MOBILE, PASSWORD, CAPTCHA));
    }

    @Test
    void shouldReturnUserWhenCredentialsMatch() {
        User user = registeredUser("pbkdf2$600000$c2FsdA==$aGFzaA==");
        when(userMapper.getUserByMobile(new Mobile(MOBILE))).thenReturn(Optional.of(user));
        when(passwordHasher.matches(PASSWORD, user.getPassword())).thenReturn(true);

        assertEquals(user, validator.authenticate(MOBILE, PASSWORD));
    }

    /**
     * 手机号不存在与密码错误必须给出同一个错误码，否则接口会变成账号枚举器。
     */
    @Test
    void shouldRejectUnknownMobileAndWrongPasswordWithSameErrorCode() {
        when(userMapper.getUserByMobile(new Mobile(MOBILE))).thenReturn(Optional.empty());
        assertCode(UserErrorCode.LOGIN_FAILED, () -> validator.authenticate(MOBILE, PASSWORD));

        User user = registeredUser("stored-hash");
        when(userMapper.getUserByMobile(new Mobile(MOBILE))).thenReturn(Optional.of(user));
        when(passwordHasher.matches(PASSWORD, "stored-hash")).thenReturn(false);
        assertCode(UserErrorCode.LOGIN_FAILED, () -> validator.authenticate(MOBILE, PASSWORD));
    }

    private static User registeredUser(String encodedPassword) {
        User user = new User();
        user.setId(9L);
        user.setMobile(new Mobile(MOBILE));
        user.setPassword(encodedPassword);
        return user;
    }

    private static void assertCode(UserErrorCode expected, Runnable action) {
        ErrorCodeException exception = assertThrows(ErrorCodeException.class, action::run);
        assertEquals(expected, exception.getErrorCode());
    }
}
