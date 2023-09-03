package cn.mklaus.app.application.product.command;

import cn.mklaus.app.application.product.command.assembler.CategoryAssembler;
import cn.mklaus.app.application.product.command.request.CategoryCreateRequest;
import cn.mklaus.app.application.product.command.request.CategoryRemoveRequest;
import cn.mklaus.app.application.product.command.request.CategoryUpdateRequest;
import cn.mklaus.app.application.product.model.CategoryDTO;
import cn.mklaus.app.domain.product.category.Category;
import cn.mklaus.app.domain.product.category.CategoryRepository;
import cn.mklaus.app.domain.product.category.CategoryValidator;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * @author klausxie
 * @since 2023/8/20
 */
@Service
@AllArgsConstructor
public class CategoryCmdService {

    private CategoryAssembler categoryAssembler;
    private CategoryValidator categoryValidator;
    private CategoryRepository categoryRepository;

    public CategoryDTO createCategory(CategoryCreateRequest req) {
        Category category = categoryAssembler.buildCategory(req);
        categoryRepository.saveCategory(category);
        return categoryAssembler.toCategoryDTO(category);
    }

    public CategoryDTO updateCategory(CategoryUpdateRequest req) {
        Category category = categoryRepository.getCategory(req.getCategoryId())
                        .orElseThrow(() -> new RuntimeException("类别不存在: " + req.getCategoryId()));
        category.setName(req.getName());
        category.setDescription(req.getDescription());
        categoryValidator.assertCategoryNameCanUse(category);
        categoryRepository.updateCategory(category);
        return categoryAssembler.toCategoryDTO(category);
    }

    public void removeCategory(CategoryRemoveRequest req) {
        Category category = categoryRepository.getCategory(req.getCategoryId())
                .orElseThrow(() -> new RuntimeException("类别不存在: " + req.getCategoryId()));
        categoryValidator.assertCategoryCanRemove(req.getCategoryId());
        categoryRepository.removeCategory(category.getId());
    }

}
