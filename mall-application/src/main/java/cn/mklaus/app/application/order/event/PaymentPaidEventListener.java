package cn.mklaus.app.application.order.event;

import cn.mklaus.app.application.order.command.OrderCmdService;
import cn.mklaus.app.domain.finance.PaymentPaidEvent;
import lombok.AllArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * @author klausxie
 * @since 2023/8/27
 */
@Component
@AllArgsConstructor
public class PaymentPaidEventListener {

    private final OrderCmdService orderCmdService;

    @EventListener(PaymentPaidEvent.class)
    public void handle(PaymentPaidEvent event) {
        orderCmdService.orderPaid(event.getOutTradeNo());
    }

}
