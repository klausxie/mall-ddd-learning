package cn.mklaus.app.domain.product.product;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * @author klausxie
 * @since 2023/9/4
 */
@Component
@AllArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductValidator productValidator;

    public Product ensureGetProduct(long productId) {
        return productRepository.getProduct(productId)
                .orElseThrow(() -> new RuntimeException("商品不存在"));
    }

    public void saveProduct(Product product) {
        product.setStatus(ProductStatus.PENDING);
        product.validate();
        productValidator.assertProductNameCanUse(product);
        productRepository.saveProduct(product);
    }

    public void updateProductInfo(Product product) {
        Product saved = ensureGetProduct(product.getId());

        if (!saved.getName().equals(product.getName())) {
            productValidator.assertProductNameCanUse(product);
        }

        saved.setName(product.getName());
        saved.setDescription(product.getDescription());
        saved.setContent(product.getContent());
        saved.setCover(product.getCover());

        productRepository.updateProduct(product);
    }

    public void removeProduct(long productId) {
        Product product = ensureGetProduct(productId);
        productValidator.assertProductCanRemove(product);
        productRepository.removeProduct(product);
    }

}
