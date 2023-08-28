package cn.mklaus.app.domain.order;


import lombok.Data;
import org.springframework.util.Assert;

import java.util.List;

/**
 * @author klausxie
 * @since 2023/8/15
 */
@Data
public class Order {

    private Long userId;
    private String no;
    private OrderStatus status;
    private List<OrderItem> items;

    private Long amount;
    private Long paymentId;

    public void changeStatusToPaid() {
        Assert.state(OrderStatus.PENDING.equals(status), "订单状态不可以修改为已支付");
        status = OrderStatus.PAID;
    }

    public Long getAmount() {
        if (amount == 0 && !items.isEmpty()) {
            amount = calculateAmount();
        }
        return amount;
    }

    public long calculateAmount() {
        return items.stream()
                .mapToLong(item -> item.getProductPrice() * item.getCount())
                .sum();
    }

}
