package cn.mklaus.app.domain.product;

import cn.mklaus.app.common.exception.Asserts;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * @author klausxie
 * @since 2023/8/16
 */
@Component
@AllArgsConstructor
public class ProductValidator {

    private final ProductMapper productMapper;

    public void assertProductExists(long productId) {
        boolean present = productMapper.getProduct(productId).isPresent();
        Asserts.state(present, ProductErrorCode.PRODUCT_NOT_EXISTS);
    }

    public void assertProductNameCanUse(Product product) {
        boolean namePresent = product.getName() != null && !product.getName().isBlank();
        Asserts.state(namePresent, ProductErrorCode.PRODUCT_NAME_IS_REQUIRED);

        productMapper.getProductByName(product.getName())
            .ifPresent(saved -> {
                boolean sameProduct = saved.getId().equals(product.getId());
                Asserts.state(sameProduct, ProductErrorCode.PRODUCT_NAME_ALREADY_EXISTS);
            });
    }

    public void assertProductCanRemove(Product product) {
        boolean neverOnSale = ProductStatus.PENDING.equals(product.getStatus());
        Asserts.state(neverOnSale, ProductErrorCode.PRODUCT_ON_SALE_CANNOT_REMOVE);
    }

}
