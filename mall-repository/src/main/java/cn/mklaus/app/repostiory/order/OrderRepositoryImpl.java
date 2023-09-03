package cn.mklaus.app.repostiory.order;

import cn.mklaus.app.domain.order.Order;
import cn.mklaus.app.domain.order.OrderRepository;
import cn.mklaus.app.repostiory.order.mapper.OrderMapper;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * @author klausxie
 * @since 2023/9/3
 */
@Component
@AllArgsConstructor
public class OrderRepositoryImpl implements OrderRepository {

    private final OrderMapper orderMapper;

    @Override
    public void saveOrder(Order order) {

    }

    @Override
    public void updateOrder(Order order) {

    }

    @Override
    public Optional<Order> getOrder(String orderNo) {
        return Optional.empty();
    }

}
