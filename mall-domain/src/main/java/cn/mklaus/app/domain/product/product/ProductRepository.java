package cn.mklaus.app.domain.product.product;

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

    void saveComment(Comment comment);

}
