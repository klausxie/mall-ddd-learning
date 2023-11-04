package cn.mklaus.app.domain.product.product;

import cn.mklaus.app.common.exception.Asserts;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * @author klausxie
 * @since 2023/8/16
 */
@Component
@AllArgsConstructor
public class ProductValidator {

    private final ProductRepository productRepository;

    public void assertProductExists(long productId) {
        boolean present = productRepository.getProduct(productId).isPresent();
        Asserts.state(present, ProductErrorCode.PRODUCT_NOT_EXISTS);
    }

    public void assertProductNameCanUse(Product product) {
        Assert.hasText(product.getName(), "商品名称不能为空");
        productRepository.getProductByName(product.getName())
                .ifPresent(saved -> {
                    boolean sameProduct = saved.getId().equals(product.getId());
                    Asserts.state(sameProduct, ProductErrorCode.PRODUCT_NAME_ALREADY_EXISTS);
                });
    }

    public void assertProductCanRemove(Product product) {
        boolean neverOnSale = ProductStatus.PENDING.equals(product.getStatus());
        Assert.state(neverOnSale, "已上线的商品不能删除");
    }

}
