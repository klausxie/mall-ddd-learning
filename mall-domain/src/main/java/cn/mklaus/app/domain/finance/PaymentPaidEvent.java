package cn.mklaus.app.domain.finance;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author klausxie
 * @since 2023/8/25
 */
@Data
@AllArgsConstructor
public class PaymentPaidEvent {

    private Long paymentId;
    private String outTradeNo;

}
