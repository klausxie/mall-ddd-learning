package cn.mklaus.app.application.product.command.request;

import lombok.Data;

/**
 * @author klausxie
 * @since 2023/8/24
 */
@Data
public class CategoryUpdateRequest {

    private Long categoryId;
    private String name;
    private String description;

}
