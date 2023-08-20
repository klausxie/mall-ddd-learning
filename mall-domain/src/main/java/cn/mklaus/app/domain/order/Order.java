package cn.mklaus.app.domain.order;


import lombok.Data;

import java.util.List;

/**
 * @author klausxie
 * @since 2023/8/15
 */
@Data
public class Order {

    private String no;
    private OrderStatus status;
    private List<OrderItem> items;

    private Long price;
    private Long paymentId;

}
