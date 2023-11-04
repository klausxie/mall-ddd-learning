package cn.mklaus.app.domain.product.category;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * @author klausxie
 * @since 2023/9/4
 */
@Component
@AllArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryValidator categoryValidator;

    public Category ensureGetCategory(long categoryId) {
        return categoryRepository.getCategory(categoryId)
                .orElseThrow(() -> new RuntimeException("类别不存在: " + categoryId));
    }

    public void saveCategory(Category category) {
        categoryValidator.assertCategoryNameCanUse(category);
        categoryRepository.saveCategory(category);
    }

    public void updateCategory(Category category) {
        categoryValidator.assertCategoryNameCanUse(category);
        categoryRepository.updateCategory(category);

        Category saved = ensureGetCategory(category.getId());
        saved.setName(category.getName());
        saved.setDescription(category.getDescription());

        categoryRepository.updateCategory(saved);
    }

    public void removeCategory(long categoryId) {
        categoryValidator.assertCategoryCanRemove(categoryId);
        categoryRepository.removeCategory(categoryId);
    }

}
