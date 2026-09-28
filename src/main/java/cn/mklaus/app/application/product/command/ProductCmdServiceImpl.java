package cn.mklaus.app.application.product.command;

import cn.mklaus.app.application.product.command.assembler.ProductAssembler;
import cn.mklaus.app.application.product.command.request.ProductCreateRequest;
import cn.mklaus.app.application.product.command.request.ProductOffSaleRequest;
import cn.mklaus.app.application.product.command.request.ProductOnSaleRequest;
import cn.mklaus.app.application.product.command.request.ProductRemoveRequest;
import cn.mklaus.app.application.product.command.request.ProductUpdateRequest;
import cn.mklaus.app.application.product.query.response.ProductInfo;
import cn.mklaus.app.domain.product.Product;
import cn.mklaus.app.domain.product.ProductMapper;
import cn.mklaus.app.domain.product.ProductService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author klausxie
 * @since 2023/11/4
 */
@Service
@Transactional
@AllArgsConstructor
public class ProductCmdServiceImpl implements ProductCmdService {

    private final ProductAssembler productAssembler;
    private final ProductMapper productMapper;
    private final ProductService productService;

    @Override
    public ProductInfo createProduct(ProductCreateRequest req) {
        Product product = productAssembler.buildProduct(req);
        productService.saveProduct(product);
        return ProductInfo.of(product);
    }

    @Override
    public void updateProduct(ProductUpdateRequest req) {
        Product product = productAssembler.buildProduct(req);
        productService.updateProductInfo(product);
    }

    @Override
    public void removeProduct(ProductRemoveRequest req) {
        productService.removeProduct(req.getProductId());
    }

    @Override
    public void onSaleProduct(ProductOnSaleRequest req) {
        Product product = productService.ensureGetProduct(req.getProductId());
        product.becomeOnSale();
        productMapper.updateProduct(product);
    }

    @Override
    public void offSaleProduct(ProductOffSaleRequest req) {
        Product product = productService.ensureGetProduct(req.getProductId());
        product.becomeOffSale();
        productMapper.updateProduct(product);
    }

}
