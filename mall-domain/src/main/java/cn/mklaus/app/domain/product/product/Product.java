package cn.mklaus.app.domain.product.product;

import lombok.Data;
import org.springframework.util.Assert;

/**
 * @author klausxie
 * @since 2023/8/15
 */
@Data
public class Product {

    private Long id;
    private Long categoryId;
    private ProductStatus status;
    private String name;
    private String description;
    private String content;
    private String cover;

    private Long price;
    private Integer inventory;

    public void updatePrice(Long price) {
        Assert.state(price > 0, "价格不能小于等于0");
        this.price = price;
    }

    private void updateInventory(int inventory) {
        Assert.state(inventory >= 0, "库存不能小于0");
        this.inventory = inventory;
    }

}
