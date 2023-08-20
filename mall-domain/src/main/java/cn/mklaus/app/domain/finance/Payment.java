package cn.mklaus.app.domain.finance;

import lombok.Data;

/**
 * @author klausxie
 * @since 2023/8/15
 */
@Data
public class Payment {

    private Long id;
    private PaymentStatus status;
    private String outTradeNo;
    private String transactionId;

}
