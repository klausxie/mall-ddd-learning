package cn.mklaus.app.domain.order;

import lombok.Data;

/**
 * @author klausxie
 * @since 2023/8/16
 */
@Data
public class OrderItem {

    private Long productId;
    private String productName;
    private String productDescription;
    private String productCover;
    private Integer count;

}
