package cn.mklaus.app.domain.product;

import cn.mklaus.app.common.exception.ErrorCodeException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * 商品上下架与价格库存不变量。校验失败必须带业务错误码，而不是 NPE 或裸异常。
 *
 * @author klaus
 * @since 2026/9/28
 */
class ProductTest {

    @Test
    void shouldBecomeOnSaleFromPending() {
        Product product = product(ProductStatus.PENDING, 100L, 1);

        product.becomeOnSale();

        assertEquals(ProductStatus.ON_SALE, product.getStatus());
    }

    @Test
    void shouldRejectOnSaleWhenAlreadyOnSale() {
        Product product = product(ProductStatus.ON_SALE, 100L, 1);

        assertCode(ProductErrorCode.PRODUCT_ALREADY_ON_SALE, product::becomeOnSale);
    }

    @Test
    void shouldRejectOnSaleWhenPriceOrInventoryIllegal() {
        assertCode(ProductErrorCode.PRODUCT_PRICE_ILLEGAL, product(ProductStatus.PENDING, 0L, 1)::becomeOnSale);
        assertCode(ProductErrorCode.PRODUCT_PRICE_ILLEGAL, product(ProductStatus.PENDING, null, 1)::becomeOnSale);
        assertCode(ProductErrorCode.PRODUCT_INVENTORY_ILLEGAL, product(ProductStatus.PENDING, 100L, -1)::becomeOnSale);
        assertCode(ProductErrorCode.PRODUCT_INVENTORY_ILLEGAL,
            product(ProductStatus.PENDING, 100L, null)::becomeOnSale);
    }

    @Test
    void shouldBecomeOffSaleFromOnSale() {
        Product product = product(ProductStatus.ON_SALE, 100L, 1);

        product.becomeOffSale();

        assertEquals(ProductStatus.OFF_SALE, product.getStatus(), "下架后应是 OFF_SALE，而不是回到 PENDING");
    }

    @Test
    void shouldRejectOffSaleWhenNotOnSale() {
        assertCode(ProductErrorCode.PRODUCT_NOT_ON_SALE, product(ProductStatus.PENDING, 100L, 1)::becomeOffSale);
        assertCode(ProductErrorCode.PRODUCT_NOT_ON_SALE, product(ProductStatus.OFF_SALE, 100L, 1)::becomeOffSale);
    }

    @Test
    void validateShouldReportPriceAndInventoryRules() {
        assertCode(ProductErrorCode.PRODUCT_PRICE_ILLEGAL, product(ProductStatus.PENDING, -1L, 1)::validate);
        assertCode(ProductErrorCode.PRODUCT_INVENTORY_ILLEGAL, product(ProductStatus.PENDING, 100L, -1)::validate);
    }

    private static void assertCode(ProductErrorCode expected, Runnable action) {
        ErrorCodeException exception = assertThrows(ErrorCodeException.class, action::run);
        assertEquals(expected, exception.getErrorCode());
    }

    private static Product product(ProductStatus status, Long price, Integer inventory) {
        Product product = new Product();
        product.setStatus(status);
        product.setName("测试商品");
        product.setPrice(price);
        product.setInventory(inventory);
        return product;
    }

}
