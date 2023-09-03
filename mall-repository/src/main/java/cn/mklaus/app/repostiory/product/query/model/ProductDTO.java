package cn.mklaus.app.repostiory.product.query.model;

import lombok.Data;

/**
 * @author klausxie
 * @since 2023/8/24
 */
@Data
public class ProductDTO {

    private Long id;
    private Long categoryId;
    private String name;
    private String description;
    private String content;
    private String cover;

    private Long price;
    private Integer inventory;

}
