package cn.mklaus.app.infrastructure.captcha;

import cn.mklaus.app.domain.common.CaptchaService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * @author klausxie
 * @since 2023/9/3
 */
@Component
@AllArgsConstructor
public class SendCloudCaptchaService implements CaptchaService {

    private final SendCloudClient client;

    @Override
    public void sendCaptcha(String mobile) {
        // generate random captcha
        // client invoke http request to send captcha to mobile
        // save {mobile:captcha} [expire: 5min] to redis
    }

    @Override
    public boolean isCaptchaValidate(String mobile, String captcha) {
        // check if exists {mobile:captcha}
        return false;
    }

}
