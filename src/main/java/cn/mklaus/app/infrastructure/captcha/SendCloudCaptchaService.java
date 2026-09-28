package cn.mklaus.app.infrastructure.captcha;

import cn.mklaus.app.domain.common.CaptchaService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 验证码服务的样例替身。
 *
 * <p>
 * 真实实现（生成随机码、调短信网关、写 Redis 并 5 分钟过期）尚未接入，
 * 这里通过 {@code mall.captcha.fixed-code} 配置一个固定验证码，让注册链路默认可跑通。
 *
 * <p>
 * 该配置留空时一律校验失败——宁可把注册拦住，也不要误放行。
 *
 * @author klausxie
 * @since 2023/9/3
 */
@Slf4j
@Component
public class SendCloudCaptchaService implements CaptchaService {

    private final String fixedCode;

    public SendCloudCaptchaService(@Value("${mall.captcha.fixed-code:}") String fixedCode) {
        this.fixedCode = fixedCode;
    }

    @Override
    public void sendCaptcha(String mobile) {
        log.info("验证码发送尚未接入真实短信网关, mobile={}", mobile);
    }

    @Override
    public boolean isCaptchaValidate(String mobile, String captcha) {
        if (fixedCode.isBlank()) {
            log.warn("mall.captcha.fixed-code 未配置，验证码校验一律失败, mobile={}", mobile);
            return false;
        }
        return fixedCode.equals(captcha);
    }

}
