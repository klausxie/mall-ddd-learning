package cn.mklaus.app.domain.finance;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author klausxie
 * @since 2023/8/27
 */
@Component
@AllArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final ThirdpartyPaymentPlatform thirdpartyPaymentPlatform;

    public Payment createPayment(String outTradeNo, Long amount) {
        Payment payment = new Payment();
        payment.setStatus(PaymentStatus.PENDING);
        payment.setOutTradeNo(outTradeNo);
        payment.setAmount(amount);
        paymentRepository.savePayment(payment);
        return payment;
    }

    public void preparePayInfo(Payment payment, PayMethod payMethod) {
        if (PaymentStatus.PENDING.equals(payment.getStatus())) {
            Map<String, Object> payInfo = thirdpartyPaymentPlatform
                    .createPayment(payment.getOutTradeNo(), payment.getAmount(), payMethod);
            payment.setPayInfo(payInfo);
            payment.setStatus(PaymentStatus.PREPARE);
            paymentRepository.updatePayment(payment);
        }
    }

}
