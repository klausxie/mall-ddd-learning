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

    public void becomeOnSale() {
        boolean onSale = ProductStatus.ON_SALE.equals(status);
        Assert.state(!onSale, "商品已上线");
        Assert.state(price > 0, "价格不能小于等于0");
        Assert.state(inventory >= 0, "库存不能小于0");
        status = ProductStatus.ON_SALE;
    }

    public void becomeOffSale() {
        boolean onSale = ProductStatus.ON_SALE.equals(status);
        Assert.state(onSale, "只有上线状态商品才可以下线");
        status = ProductStatus.PENDING;
    }

    public void validate() {
        Assert.state(price > 0, "价格不能小于等于0");
        Assert.state(inventory >= 0, "库存不能小于0");
    }

}
