package cn.mklaus.app.domain.finance;

/**
 * @author klausxie
 * @since 2023/8/16
 */
public enum PaymentStatus {

    /**
     * 待处理，待支付(已获取到第三方支付所需配置信息), 已取消，已付款，已退款
     */
    PENDING, PREPARE, CANCELLED, PAID, REFUNDED

}
