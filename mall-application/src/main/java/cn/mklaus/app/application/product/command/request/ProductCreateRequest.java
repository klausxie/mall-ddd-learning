package cn.mklaus.app.application.product.command.request;

import lombok.Data;

/**
 * @author klausxie
 * @since 2023/8/24
 */
@Data
public class ProductCreateRequest {

    private String name;
    private String description;
    private String content;
    private String cover;

    private Long price;
    private Integer inventory;

}
