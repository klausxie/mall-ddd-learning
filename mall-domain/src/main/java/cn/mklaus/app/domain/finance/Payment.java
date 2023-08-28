package cn.mklaus.app.domain.finance;

import lombok.Data;

import java.util.Map;

/**
 * @author klausxie
 * @since 2023/8/15
 */
@Data
public class Payment {

    private Long id;
    private PaymentStatus status;
    private Long amount;
    private PayMethod payMethod;
    private String outTradeNo;
    private String transactionId;
    private Map<String, Object> payInfo;

}
