package cn.mklaus.app.application.order.command;

import cn.mklaus.app.application.order.command.request.OrderCreateRequest;
import cn.mklaus.app.repostiory.order.query.model.OrderDTO;
import cn.mklaus.app.domain.order.Order;
import cn.mklaus.app.domain.order.OrderItem;
import cn.mklaus.app.domain.order.OrderNoGenerator;
import cn.mklaus.app.domain.order.OrderStatus;
import cn.mklaus.app.domain.product.product.Product;
import cn.mklaus.app.domain.product.product.ProductRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author klausxie
 * @since 2023/8/27
 */
@Component
@AllArgsConstructor
public class OrderAssembler {

    private final ProductRepository productRepository;
    private final OrderNoGenerator orderNoGenerator;

    public Order buildOrder(OrderCreateRequest request) {
        Assert.state(!request.getItems().isEmpty(), "订单商品不能为空");
        List<OrderItem> orderItems = request.getItems().stream().map(this::buildOrderItem).collect(Collectors.toList());

        Order order = new Order();
        order.setStatus(OrderStatus.PENDING);
        order.setNo(orderNoGenerator.nextOrderNo());
        order.setItems(orderItems);
        order.setAmount(order.calculateAmount());
        return order;
    }

    private OrderItem buildOrderItem(OrderCreateRequest.Item item) {
        Product product = productRepository.getProduct(item.getProductId())
                .orElseThrow(() -> new RuntimeException("商品不存在"));
        Assert.state(item.getCount() > 0, "商品数量不能小于1");

        OrderItem orderItem = new OrderItem();
        orderItem.setProductId(product.getId());
        orderItem.setPrice(product.getPrice());
        orderItem.setName(product.getName());
        orderItem.setDescription(product.getDescription());
        orderItem.setCover(product.getCover());
        orderItem.setCount(item.getCount());
        return orderItem;
    }

    public OrderDTO toOrderDTO(Order order) {
        OrderDTO dto = new OrderDTO();
        BeanUtils.copyProperties(order, dto);
        return dto;
    }

}
