package cn.mklaus.app.application.product.command;

import cn.mklaus.app.application.product.command.request.CategoryCreateRequest;
import cn.mklaus.app.application.product.command.request.CategoryUpdateRequest;
import cn.mklaus.app.application.product.model.CategoryDTO;
import cn.mklaus.app.domain.product.category.Category;
import cn.mklaus.app.domain.product.category.CategoryValidator;
import lombok.AllArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

/**
 * @author klausxie
 * @since 2023/8/24
 */
@Component
@AllArgsConstructor
public class CategoryAssembler {

    private final CategoryValidator categoryValidator;

    public Category buildCategory(CategoryCreateRequest req) {
        Category category = new Category();
        category.setName(req.getName());
        category.setDescription(req.getDescription());
        categoryValidator.assertCategoryNameCanUse(category);
        return category;
    }

    public Category buildCategory(CategoryUpdateRequest req) {
        Category category = new Category();
        category.setName(req.getName());
        category.setDescription(req.getDescription());
        categoryValidator.assertCategoryNameCanUse(category);
        return category;
    }

    public CategoryDTO toCategoryDTO(Category category) {
        CategoryDTO dto = new CategoryDTO();
        BeanUtils.copyProperties(category, dto);
        return dto;
    }

}
