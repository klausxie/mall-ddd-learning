package cn.mklaus.app.infrastructure.captcha;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 验证码替身的行为：配了固定码才放行，没配就一律拒绝。
 *
 * @author klaus
 * @since 2026/9/28
 */
class SendCloudCaptchaServiceTest {

    @Test
    void shouldAcceptFixedCodeWhenConfigured() {
        SendCloudCaptchaService captchaService = new SendCloudCaptchaService("123456");

        assertTrue(captchaService.isCaptchaValidate("13900000000", "123456"));
    }

    @Test
    void shouldRejectWrongCode() {
        SendCloudCaptchaService captchaService = new SendCloudCaptchaService("123456");

        assertFalse(captchaService.isCaptchaValidate("13900000000", "000000"));
        assertFalse(captchaService.isCaptchaValidate("13900000000", null));
    }

    @Test
    void shouldRejectEverythingWhenNotConfigured() {
        SendCloudCaptchaService captchaService = new SendCloudCaptchaService("");

        assertFalse(captchaService.isCaptchaValidate("13900000000", "123456"));
        assertFalse(captchaService.isCaptchaValidate("13900000000", ""));
    }

}
