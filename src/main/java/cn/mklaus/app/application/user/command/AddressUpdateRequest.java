package cn.mklaus.app.application.user.command;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * @author klausxie
 * @since 2023/8/16
 */
@Data
public class AddressUpdateRequest {

    @NotNull(message = "地址 ID 不能为空")
    private Long addressId;

    @NotBlank(message = "收件人不能为空")
    private String recipient;

    @NotBlank(message = "联系电话不能为空")
    private String phone;

    private String province;
    private String city;
    private String district;
    private String detail;

}
