package cn.mklaus.app.domain.finance;

import java.util.Map;

/**
 * @author klausxie
 * @since 2023/8/27
 */
public interface ThirdpartyPaymentPlatform {

    Map<String, Object> createPayment(String outTradeNo, long amount, PayMethod payMethod);

}
