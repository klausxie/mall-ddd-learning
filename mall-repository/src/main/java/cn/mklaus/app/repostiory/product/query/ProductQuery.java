package cn.mklaus.app.repostiory.product.query;

import cn.mklaus.app.common.model.Page;
import cn.mklaus.app.repostiory.product.query.condition.ProductPageCondition;
import cn.mklaus.app.repostiory.product.query.model.ProductDTO;

/**
 * @author klausxie
 * @since 2023/8/24
 */
public interface ProductQuery {

    Page<ProductDTO> pageProduct(ProductPageCondition cnd);

}
