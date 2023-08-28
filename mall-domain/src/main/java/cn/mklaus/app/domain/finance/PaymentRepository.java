package cn.mklaus.app.domain.finance;

import java.util.Optional;

/**
 * @author klausxie
 * @since 2023/8/27
 */
public interface PaymentRepository {

    Optional<Payment> getPayment(long paymentId);

    Optional<Payment> getPaymentByOutTradeNo(String outTradeNo);

    void savePayment(Payment payment);

    void updatePayment(Payment payment);

}
