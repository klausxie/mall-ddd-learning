package cn.mklaus.app.application.product.query;

import cn.mklaus.app.application.product.query.request.ProductPageRequest;
import cn.mklaus.app.common.model.Page;
import cn.mklaus.app.repostiory.product.query.model.ProductDTO;

/**
 * @author klausxie
 * @since 2023/8/24
 */
public interface ProductQueryService {

    Page<ProductDTO> pageProduct(ProductPageRequest req);

}
