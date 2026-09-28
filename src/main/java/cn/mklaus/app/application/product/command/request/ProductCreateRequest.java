package cn.mklaus.app.application.product.command.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * @author klausxie
 * @since 2023/8/24
 */
@Data
public class ProductCreateRequest {

    @NotBlank(message = "商品名称不能为空")
    private String name;

    private String description;
    private String content;
    private String cover;

    @NotNull(message = "价格不能为空")
    private Long price;

    @NotNull(message = "库存不能为空")
    private Integer inventory;

}
