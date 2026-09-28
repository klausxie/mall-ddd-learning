package cn.mklaus.app.domain.product;

import cn.mklaus.app.common.exception.Asserts;
import lombok.Data;

/**
 * @author klausxie
 * @since 2023/8/15
 */
@Data
public class Product {

    private Long id;
    private ProductStatus status;
    private String name;
    private String description;
    private String content;
    private String cover;

    private Long price;
    private Integer inventory;

    public void becomeOnSale() {
        Asserts.state(!ProductStatus.ON_SALE.equals(status), ProductErrorCode.PRODUCT_ALREADY_ON_SALE);
        validate();
        status = ProductStatus.ON_SALE;
    }

    public void becomeOffSale() {
        Asserts.state(ProductStatus.ON_SALE.equals(status), ProductErrorCode.PRODUCT_NOT_ON_SALE);
        status = ProductStatus.OFF_SALE;
    }

    /**
     * 永远成立的不变量。价格库存是包装类型，必须显式判空，否则自动拆箱会抛 NPE。
     */
    public void validate() {
        Asserts.state(price != null && price > 0, ProductErrorCode.PRODUCT_PRICE_ILLEGAL);
        Asserts.state(inventory != null && inventory >= 0, ProductErrorCode.PRODUCT_INVENTORY_ILLEGAL);
    }

}
