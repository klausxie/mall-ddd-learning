package cn.mklaus.app.common.model;

import lombok.Data;

/**
 * @author klausxie
 * @since 2023/8/20
 */
@Data
public class Pageable {

    private Integer pageNumber;
    private Integer pageSize;
    private Boolean needTotal = true;

}
