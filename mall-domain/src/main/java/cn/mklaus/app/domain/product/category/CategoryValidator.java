package cn.mklaus.app.domain.product.category;

import cn.mklaus.app.domain.product.product.ProductRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * @author klausxie
 * @since 2023/8/16
 */
@Component
@AllArgsConstructor
public class CategoryValidator {

    public final CategoryRepository categoryRepository;
    public final ProductRepository productRepository;

    public void assertCategoryExists(long categoryId) {
        boolean present = categoryRepository.getCategory(categoryId).isPresent();
        Assert.state(present, "类别不存在");
    }

    public void assertCategoryNameCanUse(Category category) {
        Assert.hasText(category.getName(), "类别名称不能为空");
        categoryRepository.getCategoryByName(category.getName())
                .ifPresent(saved -> Assert.state(saved.getId().equals(category.getId()), "类别名称已存在"));
    }

    public void assertCategoryCanRemove(long categoryId) {
        Assert.state(!productRepository.existsProductOfCategory(categoryId), "存在商品引用，不可删除");
    }

}
