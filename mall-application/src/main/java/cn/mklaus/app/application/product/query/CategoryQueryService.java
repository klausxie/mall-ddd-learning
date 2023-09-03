package cn.mklaus.app.application.product.query;

import cn.mklaus.app.application.product.query.request.CategoryPageRequest;
import cn.mklaus.app.common.model.Page;
import cn.mklaus.app.repostiory.product.query.CategoryQuery;
import cn.mklaus.app.repostiory.product.query.model.CategoryDTO;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author klausxie
 * @since 2023/8/20
 */
@Service
@AllArgsConstructor
public class CategoryQueryService {

    private final CategoryQuery categoryQuery;

    public Page<CategoryDTO> pageCategory(CategoryPageRequest req) {
        return categoryQuery.pageCategory(req.getKeyword(), req);
    }

    public List<CategoryDTO> listAll() {
        return categoryQuery.listAll();
    }

}
