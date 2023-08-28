package cn.mklaus.app.domain.product.product;

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
        Assert.state(productRepository.getProduct(productId).isPresent(), "商品不存在");
    }

    public void assertProductNameCanUse(Product product) {
        Assert.hasText(product.getName(), "商品名称不能为空");
        productRepository.getProductByName(product.getName())
                .ifPresent(saved -> Assert.state(saved.getId().equals(product.getId()), "商品名称已存在"));
    }

    public void assertProductCanRemove(Product product) {
        boolean neverOnSale = ProductStatus.PENDING.equals(product.getStatus());
        Assert.state(neverOnSale, "已上线的商品不能删除");
    }

}
