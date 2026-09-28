package cn.mklaus.app.domain.product;

import cn.mklaus.app.common.exception.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author klausxie
 * @since 2023/11/4
 */
@Getter
@AllArgsConstructor
public enum ProductErrorCode implements ErrorCode {

    /**
     * 商品不存在
     */
    PRODUCT_NOT_EXISTS(60001, "商品不存在"),

    /**
     * 商品名称已存在
     */
    PRODUCT_NAME_ALREADY_EXISTS(60002, "商品名称已存在"),

    /**
     * 商品名称不能为空
     */
    PRODUCT_NAME_IS_REQUIRED(60003, "商品名称不能为空"),

    /**
     * 已上架过的商品不能删除
     */
    PRODUCT_ON_SALE_CANNOT_REMOVE(60004, "已上架过的商品不能删除"),

    /**
     * 价格非法
     */
    PRODUCT_PRICE_ILLEGAL(60005, "价格不能小于等于0"),

    /**
     * 库存非法
     */
    PRODUCT_INVENTORY_ILLEGAL(60006, "库存不能小于0"),

    /**
     * 商品已上线
     */
    PRODUCT_ALREADY_ON_SALE(60007, "商品已上线"),

    /**
     * 商品不在上线状态
     */
    PRODUCT_NOT_ON_SALE(60008, "只有上线状态商品才可以下线")

    ;

    private final int code;
    private final String template;

}
