package cn.mklaus.app.domain.product.product;

import lombok.Builder;
import lombok.Data;

import java.util.Date;

/**
 * @author klausxie
 * @since 2023/8/15
 */
@Data
@Builder
public class Comment {

    private Long id;
    private Long orderId;
    private Long productId;
    private Long userId;
    private String content;
    private Date createTime;
    private Date updateTime;

}
