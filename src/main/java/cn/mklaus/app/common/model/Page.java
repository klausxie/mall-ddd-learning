package cn.mklaus.app.common.model;

import lombok.Data;

import java.util.List;

/**
 * @author klausxie
 * @since 2023/8/20
 */
@Data
public class Page<T> {

    private Integer curPage;
    private Integer pageSize;
    private Long total;
    private List<T> records;

    /**
     * 组装分页结果，保证四个字段一次性给全。
     */
    public static <T> Page<T> of(Integer curPage, Integer pageSize, long total, List<T> records) {
        Page<T> page = new Page<>();
        page.curPage = curPage;
        page.pageSize = pageSize;
        page.total = total;
        page.records = records;
        return page;
    }

}
