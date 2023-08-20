package cn.mklaus.app.common.model;

import lombok.Data;

import java.util.List;

/**
 * @author klausxie
 * @since 2023/8/20
 */
@Data
public class Page<T> {

    private Integer pageNumber;
    private Integer pageSize;
    private Long total;
    private List<T> records;

}
