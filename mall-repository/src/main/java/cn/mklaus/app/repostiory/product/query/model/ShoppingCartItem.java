package cn.mklaus.app.repostiory.product.query.model;

import cn.mklaus.app.application.product.model.ProductDTO;
import lombok.Data;

/**
 * @author klausxie
 * @since 2023/8/25
 */
@Data
public class ShoppingCartItem extends ProductDTO {

    private String categoryName;
    private Integer count;

}
