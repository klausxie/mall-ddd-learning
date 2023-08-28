package cn.mklaus.app.common.model;

import lombok.Data;
import org.springframework.util.Assert;

/**
 * @author klausxie
 * @since 2023/8/20
 */
@Data
public class Pageable {

    private Integer pageNumber = 1;
    private Integer pageSize = 10;
    private Boolean needTotal = true;

    public int getOffset() {
        Assert.state(pageSize > 0, "pageSize不能小于0");
        Assert.state(pageNumber > 0, "pageNumber不能小于0");
        return (pageNumber - 1) * pageSize;
    }

}
