package cn.mklaus.app.application.product.query;

import cn.mklaus.app.application.product.query.request.ProductPageRequest;
import cn.mklaus.app.application.product.query.response.ProductInfo;
import cn.mklaus.app.common.model.Page;

/**
 * @author klausxie
 * @since 2023/8/24
 */
public interface ProductQueryService {

    Page<ProductInfo> pageProduct(ProductPageRequest req);

}
