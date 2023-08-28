package cn.mklaus.app.application.payment;

import cn.mklaus.app.application.payment.command.PaymentPrepareRequest;
import cn.mklaus.app.application.payment.model.PaymentCallbackInfo;
import cn.mklaus.app.application.payment.model.PaymentDTO;
import cn.mklaus.app.domain.common.EventPublisher;
import cn.mklaus.app.domain.finance.*;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * @author klausxie
 * @since 2023/8/27
 */
@Component
@AllArgsConstructor
public class PaymentCmdService {

    private final PaymentRepository paymentRepository;
    private final PaymentService paymentService;
    private final PaymentAssembler paymentAssembler;
    private final EventPublisher eventPublisher;

    public PaymentDTO preparePayment(PaymentPrepareRequest req) {
        Payment payment = paymentRepository.getPayment(req.getPaymentId())
                .orElseThrow(() -> new RuntimeException("支付单不存在"));
        paymentService.preparePayInfo(payment, req.getPayMethod());
        Assert.state(PaymentStatus.PREPARE.equals(payment.getStatus()), "支付单状态异常");
        return paymentAssembler.toPaymentDTO(payment);
    }

    public void handlePaymentCallback(PaymentCallbackInfo info) {
        Payment payment = paymentRepository.getPaymentByOutTradeNo(info.getOutTradeNo())
                .orElseThrow(() -> new RuntimeException("支付单不存在"));
        payment.setStatus(PaymentStatus.PAID);
        paymentRepository.updatePayment(payment);
        PaymentPaidEvent paymentPaidEvent = new PaymentPaidEvent(payment.getId(), payment.getOutTradeNo());
        eventPublisher.publish(paymentPaidEvent);
    }

}
