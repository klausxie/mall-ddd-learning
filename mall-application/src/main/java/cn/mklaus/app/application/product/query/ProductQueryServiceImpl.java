package cn.mklaus.app.application.product.query;

import cn.mklaus.app.application.product.query.request.ProductPageRequest;
import cn.mklaus.app.common.model.Page;
import cn.mklaus.app.repostiory.product.query.ProductQuery;
import cn.mklaus.app.repostiory.product.query.model.ProductDTO;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * @author klausxie
 * @since 2023/11/4
 */
@Service
@AllArgsConstructor
public class ProductQueryServiceImpl implements ProductQueryService {

    private final ProductQuery productQuery;

    @Override
    public Page<ProductDTO> pageProduct(ProductPageRequest req) {
        return productQuery.pageProduct(req.buildCondition());
    }

}
