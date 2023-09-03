package cn.mklaus.app.repostiory.product;

import cn.mklaus.app.domain.product.category.Category;
import cn.mklaus.app.domain.product.category.CategoryRepository;
import cn.mklaus.app.repostiory.product.mapper.CategoryMapper;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * @author klausxie
 * @since 2023/8/24
 */
@Component
@AllArgsConstructor
public class CategoryRepositoryImpl implements CategoryRepository {

    private final CategoryMapper categoryMapper;

    @Override
    public Optional<Category> getCategory(long id) {
        return Optional.empty();
    }

    @Override
    public Optional<Category> getCategoryByName(String categoryName) {
        return Optional.empty();
    }

    @Override
    public void saveCategory(Category category) {

    }

    @Override
    public void updateCategory(Category category) {

    }

    @Override
    public void removeCategory(Long categoryId) {

    }
}
