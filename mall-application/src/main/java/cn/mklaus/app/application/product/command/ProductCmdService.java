package cn.mklaus.app.application.product.command;

import cn.mklaus.app.application.product.command.request.*;
import cn.mklaus.app.application.product.model.ProductDTO;
import cn.mklaus.app.application.product.query.request.ProductPageRequest;
import cn.mklaus.app.domain.product.category.CategoryValidator;
import cn.mklaus.app.domain.product.product.Product;
import cn.mklaus.app.domain.product.product.ProductRepository;
import cn.mklaus.app.domain.product.product.ProductValidator;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * @author klausxie
 * @since 2023/8/24
 */
@Service
@AllArgsConstructor
public class ProductCmdService {

    private final ProductAssembler productAssembler;
    private final ProductValidator productValidator;
    private final CategoryValidator categoryValidator;
    private final ProductRepository productRepository;

    public Product ensureGetProduct(long productId) {
        return productRepository.getProduct(productId)
                .orElseThrow(() -> new RuntimeException("商品不存在"));
    }

    public ProductDTO createProduct(ProductCreateRequest req) {
        Product product = productAssembler.buildProduct(req);
        productRepository.saveProduct(product);
        return productAssembler.toProductDTO(product);
    }

    public void updateProduct(ProductUpdateRequest req) {
        Product product = ensureGetProduct(req.getProductId());
        if (!product.getCategoryId().equals(req.getCategoryId())) {
            categoryValidator.assertCategoryExists(req.getCategoryId());
            product.setCategoryId(req.getCategoryId());
        }

        product.setName(req.getName());
        product.setDescription(req.getDescription());
        product.setContent(req.getContent());
        product.setCover(req.getCover());
        productValidator.assertProductNameCanUse(product);
        productRepository.updateProduct(product);
    }

    public void removeProduct(ProductRemoveRequest req) {
        Product product = ensureGetProduct(req.getProductId());
        productValidator.assertProductCanRemove(product);
        productRepository.removeProduct(product);
    }

    public void onSaleProduct(ProductOnSaleRequest req) {
        Product product = ensureGetProduct(req.getProductId());
        product.becomeOnSale();
        productRepository.updateProduct(product);
    }

    public void offSaleProduct(ProductOffSaleRequest req) {
        Product product = ensureGetProduct(req.getProductId());
        product.becomeOffSale();
        productRepository.updateProduct(product);
    }

}
