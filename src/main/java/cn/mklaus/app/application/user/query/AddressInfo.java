package cn.mklaus.app.application.user.query;

import cn.mklaus.app.domain.user.Address;

/**
 * 收货地址响应模型（record：不可变、无样板）。
 *
 * @author klaus
 * @since 2026/9/28
 */
public record AddressInfo(Long id, String recipient, String phone, String province, String city, String district,
    String detail) {

    public static AddressInfo of(Address address) {
        return new AddressInfo(address.getId(), address.getRecipient(), address.getPhone(), address.getProvince(),
            address.getCity(), address.getDistrict(), address.getDetail());
    }

}
