package cn.mklaus.app.application.product.query.request;

import cn.mklaus.app.common.model.Pageable;
import lombok.Data;

/**
 * @author klausxie
 * @since 2023/8/24
 */
@Data
public class CategoryPageRequest extends Pageable {

    private String keyword;

}
