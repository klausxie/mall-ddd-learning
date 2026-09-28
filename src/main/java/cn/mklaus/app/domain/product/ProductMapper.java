package cn.mklaus.app.domain.product;

import cn.mklaus.app.domain.product.query.condition.ProductPageCondition;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Optional;

/**
 * @author klausxie
 * @since 2023/8/16
 */
@Mapper
public interface ProductMapper {

    Optional<Product> getProduct(long productId);

    Optional<Product> getProductByName(String name);

    void saveProduct(Product product);

    void updateProduct(Product product);

    void removeProduct(Product product);

    /** 分页取记录，分页参数用内部的 offset / size。 */
    List<Product> listProduct(ProductPageCondition cnd);

    /** 与 {@link #listProduct} 用同一套过滤条件统计总数。 */
    long countProduct(ProductPageCondition cnd);

}
