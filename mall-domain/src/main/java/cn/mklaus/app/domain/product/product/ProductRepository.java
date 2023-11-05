package cn.mklaus.app.domain.product.product;

import cn.mklaus.app.common.model.Page;
import cn.mklaus.app.domain.product.product.query.condition.ProductPageCondition;

import java.util.Optional;

/**
 * @author klausxie
 * @since 2023/8/16
 */
public interface ProductRepository {

    boolean existsProductOfCategory(long categoryId);

    Optional<Product> getProduct(long prodcutId);

    Optional<Product> getProductByName(String name);

    void saveProduct(Product product);

    void updateProduct(Product product);

    void removeProduct(Product product);

    Page<Product> pageProduct(ProductPageCondition cnd);

}
