package cn.mklaus.app.application.order.command;

import cn.mklaus.app.application.order.command.request.OrderCreateRequest;
import cn.mklaus.app.repostiory.order.query.model.OrderDTO;
import cn.mklaus.app.common.auth.Context;
import cn.mklaus.app.domain.common.EventPublisher;
import cn.mklaus.app.domain.finance.Payment;
import cn.mklaus.app.domain.finance.PaymentService;
import cn.mklaus.app.domain.order.Order;
import cn.mklaus.app.domain.order.OrderPaidEvent;
import cn.mklaus.app.domain.order.OrderRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * @author klausxie
 * @since 2023/8/27
 */
@Component
@AllArgsConstructor
public class OrderCmdService {

    private final OrderRepository orderRepository;
    private final OrderAssembler orderAssembler;
    private final PaymentService paymentService;
    private final EventPublisher eventPublisher;

    public OrderDTO createOrder(OrderCreateRequest req) {
        Order order = orderAssembler.buildOrder(req);
        Payment payment = paymentService.createPayment(order.getNo(), order.getAmount());
        order.setPaymentId(payment.getId());
        order.setUserId(Context.currentOperator().getId());
        orderRepository.saveOrder(order);
        return orderAssembler.toOrderDTO(order);
    }

    public void orderPaid(String outTradeNo) {
        Order order = orderRepository.getOrder(outTradeNo)
                .orElseThrow(() -> new RuntimeException("订单不存在"));
        order.changeStatusToPaid();
        orderRepository.updateOrder(order);
        eventPublisher.publish(new OrderPaidEvent(order));
    }

}
