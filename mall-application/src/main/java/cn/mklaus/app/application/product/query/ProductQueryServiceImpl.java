package cn.mklaus.app.application.product.query;

import cn.mklaus.app.application.product.query.request.ProductPageRequest;
import cn.mklaus.app.common.model.Page;
import cn.mklaus.app.domain.product.product.Product;
import cn.mklaus.app.domain.product.product.ProductRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * @author klausxie
 * @since 2023/11/4
 */
@Service
@AllArgsConstructor
public class ProductQueryServiceImpl implements ProductQueryService {

    private final ProductRepository productRepository;

    @Override
    public Page<Product> pageProduct(ProductPageRequest req) {
        return productRepository.pageProduct(req.buildCondition());
    }

}
