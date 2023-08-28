package cn.mklaus.app.application.product.command.request;

import lombok.Data;

/**
 * @author klausxie
 * @since 2023/8/25
 */
@Data
public class ShoppingCartSaveRequest {

    private Long productId;
    private Integer count;

}
