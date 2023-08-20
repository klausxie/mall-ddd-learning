package cn.mklaus.app.domain.finance;

/**
 * @author klausxie
 * @since 2023/8/16
 */
public enum PaymentStatus {

    /**
     * 待支付，已取消，已付款，已退款
     */
    PENDING, CANCELLED, PAID, REFUNDED

}
