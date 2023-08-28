package cn.mklaus.app.application.product.command.request;

import cn.mklaus.app.domain.product.product.ProductStatus;
import lombok.Data;

/**
 * @author klausxie
 * @since 2023/8/24
 */
@Data
public class ProductCreateRequest {

    private Long categoryId;
    private String name;
    private String description;
    private String content;
    private String cover;

    private Long price;
    private Integer inventory;

}
