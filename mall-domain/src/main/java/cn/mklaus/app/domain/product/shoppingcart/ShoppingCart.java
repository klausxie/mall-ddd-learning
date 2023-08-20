package cn.mklaus.app.domain.product.shoppingcart;

import lombok.Data;


/**
 * @author klausxie
 * @since 2023/8/16
 */
@Data
public class ShoppingCart {

    private Long userId;
    private Long productId;
    private Integer count;

}
