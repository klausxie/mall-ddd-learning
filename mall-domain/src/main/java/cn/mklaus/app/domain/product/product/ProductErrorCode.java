package cn.mklaus.app.domain.product.product;

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
    PRODUCT_NOT_EXISTS(60001, "product not exists"),

    /**
     * 商品名称已存在
     */
    PRODUCT_NAME_ALREADY_EXISTS(60001, "product name already exists")
    ;

    private final int code;
    private final String template;
}
