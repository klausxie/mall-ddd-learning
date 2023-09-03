package cn.mklaus.app.application.product.query;

import cn.mklaus.app.application.product.query.request.ProductPageRequest;
import cn.mklaus.app.common.model.Page;
import cn.mklaus.app.repostiory.product.query.ProductQuery;
import cn.mklaus.app.repostiory.product.query.model.ProductDTO;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * @author klausxie
 * @since 2023/8/24
 */
@Component
@AllArgsConstructor
public class ProductQueryService {

    private final ProductQuery productQuery;

    public Page<ProductDTO> pageProduct(ProductPageRequest req) {
        return productQuery.pageProduct(req.buildCondition());
    }

}
