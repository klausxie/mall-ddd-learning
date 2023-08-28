package cn.mklaus.app.application.payment.command;

import cn.mklaus.app.domain.finance.PayMethod;
import lombok.Data;

/**
 * @author klausxie
 * @since 2023/8/27
 */
@Data
public class PaymentPrepareRequest {

    private Long paymentId;
    private PayMethod payMethod;

}
