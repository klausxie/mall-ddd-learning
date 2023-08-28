package cn.mklaus.app.application.payment.model;

import lombok.Data;

/**
 * @author klausxie
 * @since 2023/8/28
 */
@Data
public class PaymentCallbackInfo {

    private String outTradeNo;
    private String transactionId;

}
