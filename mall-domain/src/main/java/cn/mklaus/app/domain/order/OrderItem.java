package cn.mklaus.app.domain.order;

import lombok.Data;

/**
 * 作为产品时为实体，作为订单项时为值对象
 *
 * @author klausxie
 * @since 2023/8/16
 */
@Data
public class OrderItem {

    private Long productId;
    private Long price;
    private String name;
    private String description;
    private String cover;
    private Integer count;

}
