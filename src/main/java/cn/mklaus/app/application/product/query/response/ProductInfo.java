package cn.mklaus.app.application.product.query.response;

import cn.mklaus.app.domain.product.Product;
import cn.mklaus.app.domain.product.ProductStatus;
import lombok.Builder;
import lombok.Data;

/**
 * 商品信息响应模型。
 *
 * @author klaus
 * @since 2026/9/28
 */
@Data
@Builder
public class ProductInfo {

    private Long id;
    private ProductStatus status;
    private String name;
    private String description;
    private String content;
    private String cover;
    private Long price;
    private Integer inventory;

    public static ProductInfo of(Product product) {
        return ProductInfo.builder()
            .id(product.getId())
            .status(product.getStatus())
            .name(product.getName())
            .description(product.getDescription())
            .content(product.getContent())
            .cover(product.getCover())
            .price(product.getPrice())
            .inventory(product.getInventory())
            .build();
    }

}
