package cn.mklaus.app.application.product.command;

import cn.mklaus.app.application.product.command.assembler.CategoryAssembler;
import cn.mklaus.app.application.product.command.request.CategoryCreateRequest;
import cn.mklaus.app.application.product.command.request.CategoryRemoveRequest;
import cn.mklaus.app.application.product.command.request.CategoryUpdateRequest;
import cn.mklaus.app.domain.product.category.Category;
import cn.mklaus.app.domain.product.category.CategoryService;
import cn.mklaus.app.repostiory.product.query.model.CategoryDTO;
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
    private final CategoryService categoryService;

    public CategoryDTO createCategory(CategoryCreateRequest req) {
        Category category = categoryAssembler.buildCategory(req);
        categoryService.saveCategory(category);
        return categoryAssembler.toCategoryDTO(category);
    }

    public CategoryDTO updateCategory(CategoryUpdateRequest req) {
        Category category = categoryAssembler.buildCategory(req);
        categoryService.updateCategory(category);
        return categoryAssembler.toCategoryDTO(category);
    }

    public void removeCategory(CategoryRemoveRequest req) {
        categoryService.removeCategory(req.getCategoryId());
    }

}
