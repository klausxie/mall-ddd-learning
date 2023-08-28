package cn.mklaus.app.application.payment;

import cn.mklaus.app.application.payment.model.PaymentDTO;
import cn.mklaus.app.domain.finance.Payment;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * @author klausxie
 * @since 2023/8/27
 */
@Component
@AllArgsConstructor
public class PaymentAssembler {

    public PaymentDTO toPaymentDTO(Payment payment) {
        return new PaymentDTO();
    }

}
