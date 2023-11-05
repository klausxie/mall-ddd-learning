package cn.mklaus.app.application.product.query;

import cn.mklaus.app.application.product.query.request.ProductPageRequest;
import cn.mklaus.app.common.model.Page;
import cn.mklaus.app.domain.product.product.Product;

/**
 * @author klausxie
 * @since 2023/8/24
 */
public interface ProductQueryService {

    Page<Product> pageProduct(ProductPageRequest req);

}
