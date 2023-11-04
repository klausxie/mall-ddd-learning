package cn.mklaus.app.application.product.command.assembler;

import cn.mklaus.app.application.product.command.request.CategoryCreateRequest;
import cn.mklaus.app.application.product.command.request.CategoryUpdateRequest;
import cn.mklaus.app.domain.product.category.Category;
import cn.mklaus.app.repostiory.product.query.model.CategoryDTO;
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


    public Category buildCategory(CategoryCreateRequest req) {
        Category category = new Category();
        category.setName(req.getName());
        category.setDescription(req.getDescription());
        return category;
    }

    public Category buildCategory(CategoryUpdateRequest req) {
        Category category = new Category();
        category.setId(req.getCategoryId());
        category.setName(req.getName());
        category.setDescription(req.getDescription());
        return category;
    }

    public CategoryDTO toCategoryDTO(Category category) {
        CategoryDTO dto = new CategoryDTO();
        BeanUtils.copyProperties(category, dto);
        return dto;
    }

}
