package cn.mklaus.app.domain.common;

/**
 * @author klausxie
 * @since 2023/8/20
 */
public interface CaptchaService {

    void sendCaptcha(String mobile);

    boolean isCaptchaValidate(String mobile, String captcha);

}
