package cn.mklaus.app.repostiory.product.query;

import cn.mklaus.app.repostiory.product.query.model.CategoryDTO;
import cn.mklaus.app.common.model.Page;
import cn.mklaus.app.common.model.Pageable;

import java.util.List;

/**
 * @author klausxie
 * @since 2023/8/24
 */
public interface CategoryQuery {

    Page<CategoryDTO> pageCategory(String keyword, Pageable pageable);

    List<CategoryDTO> listAll();

}
