package cn.mklaus.app.domain.order;

import java.util.Optional;

/**
 * @author klausxie
 * @since 2023/8/27
 */
public interface OrderRepository {

    void saveOrder(Order order);

    void updateOrder(Order order);

    Optional<Order> getOrder(String orderNo);

}
