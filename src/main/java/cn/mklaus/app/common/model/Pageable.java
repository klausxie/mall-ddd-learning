package cn.mklaus.app.common.model;

import cn.mklaus.app.common.exception.Asserts;
import cn.mklaus.app.common.exception.CommonErrorCode;
import lombok.Data;

/**
 * 分页请求基类。字段是可空的包装类型，取值前必须判空，否则自动拆箱会抛 NPE。
 *
 * @author klausxie
 * @since 2023/8/20
 */
@Data
public class Pageable {

    private Integer curPage = 1;
    private Integer pageSize = 10;

    public int getOffset() {
        Asserts.state(curPage != null && curPage > 0, CommonErrorCode.CUR_PAGE_ILLEGAL);
        Asserts.state(pageSize != null && pageSize > 0, CommonErrorCode.PAGE_SIZE_ILLEGAL);
        return (curPage - 1) * pageSize;
    }

}
