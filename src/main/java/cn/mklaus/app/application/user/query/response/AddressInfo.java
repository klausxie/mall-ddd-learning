package cn.mklaus.app.application.user.query.response;

import cn.mklaus.app.domain.user.Address;
import lombok.Builder;
import lombok.Data;

/**
 * 收货地址响应模型。
 *
 * @author klaus
 * @since 2026/9/28
 */
@Data
@Builder
public class AddressInfo {

    private Long id;
    private String recipient;
    private String phone;
    private String province;
    private String city;
    private String district;
    private String detail;

    public static AddressInfo of(Address address) {
        return AddressInfo.builder()
            .id(address.getId())
            .recipient(address.getRecipient())
            .phone(address.getPhone())
            .province(address.getProvince())
            .city(address.getCity())
            .district(address.getDistrict())
            .detail(address.getDetail())
            .build();
    }

}
