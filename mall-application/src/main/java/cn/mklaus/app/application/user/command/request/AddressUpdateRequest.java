package cn.mklaus.app.application.user.command.request;

import lombok.Data;

/**
 * @author klausxie
 * @since 2023/8/16
 */
@Data
public class AddressUpdateRequest {

    private Long addressId;
    private String recipient;
    private String phone;

    private String province;
    private String city;
    private String district;
    private String detail;

}
