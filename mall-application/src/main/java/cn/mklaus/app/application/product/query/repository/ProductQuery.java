package cn.mklaus.app.application.product.query.repository;

import cn.mklaus.app.application.product.model.ProductDTO;
import cn.mklaus.app.application.product.query.repository.condition.ProductPageCondition;
import cn.mklaus.app.common.model.Page;

/**
 * @author klausxie
 * @since 2023/8/24
 */
public interface ProductQuery {

    Page<ProductDTO> pageProduct(ProductPageCondition cnd);

}
