package cn.mklaus.app.application.product.query.request;

import cn.mklaus.app.common.model.Pageable;
import cn.mklaus.app.domain.product.product.ProductStatus;
import cn.mklaus.app.repostiory.product.query.condition.ProductPageCondition;
import lombok.Data;

/**
 * @author klausxie
 * @since 2023/8/24
 */
@Data
public class ProductPageRequest extends Pageable {

    private String keyword;
    private Integer categoryId;
    private ProductStatus status;

    public ProductPageCondition buildCondition() {
        return ProductPageCondition.builder()
                .keyword(keyword)
                .categoryId(categoryId)
                .status(status)
                .offset(getOffset())
                .size(getPageSize())
                .build();
    }

}
