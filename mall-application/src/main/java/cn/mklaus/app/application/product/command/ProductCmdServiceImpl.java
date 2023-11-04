package cn.mklaus.app.application.product.command;

import cn.mklaus.app.application.product.command.assembler.ProductAssembler;
import cn.mklaus.app.application.product.command.request.*;
import cn.mklaus.app.domain.product.product.Product;
import cn.mklaus.app.domain.product.product.ProductRepository;
import cn.mklaus.app.domain.product.product.ProductService;
import cn.mklaus.app.repostiory.product.query.model.ProductDTO;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * @author klausxie
 * @since 2023/11/4
 */
@Service
@AllArgsConstructor
public class ProductCmdServiceImpl implements ProductCmdService {

    private final ProductAssembler productAssembler;
    private final ProductRepository productRepository;
    private final ProductService productService;

    @Override
    public ProductDTO createProduct(ProductCreateRequest req) {
        Product product = productAssembler.buildProduct(req);
        productService.saveProduct(product);
        return productAssembler.toProductDTO(product);
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
        productRepository.updateProduct(product);
    }

    @Override
    public void offSaleProduct(ProductOffSaleRequest req) {
        Product product = productService.ensureGetProduct(req.getProductId());
        product.becomeOffSale();
        productRepository.updateProduct(product);
    }

}