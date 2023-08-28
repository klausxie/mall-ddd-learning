package cn.mklaus.app.domain.order;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author klausxie
 * @since 2023/8/27
 */
@Data
@AllArgsConstructor
public class OrderPaidEvent {

    private Order order;

}
