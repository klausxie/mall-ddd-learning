package cn.mklaus.app.infrastructure.repository.product;

import cn.mklaus.app.application.product.model.CategoryDTO;
import cn.mklaus.app.application.product.query.repository.CategoryQuery;
import cn.mklaus.app.common.model.Page;
import cn.mklaus.app.common.model.Pageable;
import cn.mklaus.app.infrastructure.repository.product.mapper.CategoryMapper;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author klausxie
 * @since 2023/8/24
 */
@Component
@AllArgsConstructor
public class CategoryQueryImpl implements CategoryQuery {

    private final CategoryMapper categoryMapper;

    @Override
    public Page<CategoryDTO> pageCategory(String keyword, Pageable pageable) {
        return null;
    }

    @Override
    public List<CategoryDTO> listAll() {
        return null;
    }

}
