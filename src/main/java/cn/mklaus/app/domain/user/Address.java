package cn.mklaus.app.domain.user;

import cn.mklaus.app.common.exception.Asserts;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 收货地址实体。
 *
 * <p>
 * 三个构造器注解都是**必需的**，别删：{@code @Builder} 让调用方按名字装配，
 * {@code @NoArgsConstructor} + {@code @AllArgsConstructor} 保证 {@code @Builder} 有全参构造器可用、
 * 同时留一个无参构造器给 MyBatis。
 *
 * <p>
 * 只有 {@code @Builder} 时 Lombok 会把无参构造器"顶掉"，MyBatis 只能退回构造器自动映射：
 * 实体字段与 resultMap 的列一旦对不上，抛的是
 * {@code Constructor auto-mapping of 'Address(...)' failed}——报错和真因（漏字段）完全不搭。
 * 有 {@code @NoArgsConstructor} 后走常规 setter 映射，漏字段的行为与 {@code User} 一致，
 * 再由 {@code EntityMappingTest} 在构建期报出来。
 *
 * @author klausxie
 * @since 2023/8/15
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
