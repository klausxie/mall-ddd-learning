package cn.mklaus.app.domain.product.category;

import java.util.Optional;

/**
 * @author klausxie
 * @since 2023/8/16
 */
public interface CategoryRepository {

    Optional<Category> getCategory(long id);

    Optional<Category> getCategoryByName(String categoryName);

    void saveCategory(Category category);

    void updateCategory(Category category);

    void removeCategory(Long categoryId);

}
