package cn.mklaus.app.domain.product;

import cn.mklaus.app.common.exception.ErrorCodeException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * @author klausxie
 * @since 2023/9/4
 */
@Component
@AllArgsConstructor
public class ProductService {

    private final ProductMapper productMapper;
    private final ProductValidator productValidator;

    public Product ensureGetProduct(long productId) {
        return productMapper.getProduct(productId)
            .orElseThrow(() -> new ErrorCodeException(ProductErrorCode.PRODUCT_NOT_EXISTS));
    }

    public void saveProduct(Product product) {
        product.setStatus(ProductStatus.PENDING);
        product.validate();
        productValidator.assertProductNameCanUse(product);
        productMapper.saveProduct(product);
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

        productMapper.updateProduct(saved);
    }

    public void removeProduct(long productId) {
        Product product = ensureGetProduct(productId);
        productValidator.assertProductCanRemove(product);
        productMapper.removeProduct(product);
    }

}
