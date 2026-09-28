package cn.mklaus.app.domain.user;

import cn.mklaus.app.common.exception.Asserts;
import lombok.Builder;
import lombok.Data;

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

    /**
     * 永远成立的不变量。
     */
    public void validate() {
        Asserts.state(recipient != null && !recipient.isBlank(), UserErrorCode.RECIPIENT_IS_REQUIRED);
    }

    /**
     * 按用例生效的归属规则：只有地址的主人能改它。
     */
    public void assertOwnedBy(Long operatorId) {
        Asserts.state(userId != null && userId.equals(operatorId), UserErrorCode.NO_PERMISSION);
    }

}
