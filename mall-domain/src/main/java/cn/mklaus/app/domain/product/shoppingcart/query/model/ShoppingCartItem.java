package cn.mklaus.app.domain.product.shoppingcart.query.model;

import lombok.Data;

/**
 * @author klausxie
 * @since 2023/8/25
 */
@Data
public class ShoppingCartItem {

    private String productId;
    private String productName;
    private String productCover;
    private Integer count;

}
