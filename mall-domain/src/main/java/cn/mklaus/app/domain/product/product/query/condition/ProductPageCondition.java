package cn.mklaus.app.domain.product.product.query.condition;

import cn.mklaus.app.domain.product.product.ProductStatus;
import lombok.Builder;
import lombok.Data;

/**
 * @author klausxie
 * @since 2023/8/24
 */
@Data
@Builder
public class ProductPageCondition {
    private String keyword;
    private ProductStatus status;
    private Integer offset;
    private Integer size;
}
