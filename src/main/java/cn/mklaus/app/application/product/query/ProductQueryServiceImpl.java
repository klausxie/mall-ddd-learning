package cn.mklaus.app.application.product.query;

import cn.mklaus.app.application.product.query.request.ProductPageRequest;
import cn.mklaus.app.application.product.query.response.ProductInfo;
import cn.mklaus.app.common.model.Page;
import cn.mklaus.app.domain.product.ProductMapper;
import cn.mklaus.app.domain.product.query.condition.ProductPageCondition;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author klausxie
 * @since 2023/11/4
 */
@Service
@AllArgsConstructor
public class ProductQueryServiceImpl implements ProductQueryService {

    private final ProductMapper productMapper;

    @Override
    public Page<ProductInfo> pageProduct(ProductPageRequest req) {
        ProductPageCondition cnd = req.buildCondition();

        long total = productMapper.countProduct(cnd);
        List<ProductInfo> records = productMapper.listProduct(cnd).stream()
            .map(ProductInfo::of)
            .toList();
        return Page.of(req.getCurPage(), req.getPageSize(), total, records);
    }

}
