package cn.mklaus.app.domain.user;

import lombok.Builder;
import lombok.Data;
import org.springframework.util.Assert;

/**
 * @author klausxie
 * @since 2023/8/15
 */
@Data
@Builder
public class Address {

    private Long id;
    private Long userId;
    private String recipient;
    private String phone;

    private String province;
    private String city;
    private String district;
    private String detail;

    public void validate() {
        Assert.hasText(recipient, "收件人不能为空");
    }

}
