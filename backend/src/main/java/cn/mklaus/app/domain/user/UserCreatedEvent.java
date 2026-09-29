package cn.mklaus.app.domain.user;

import lombok.Data;

/**
 * 用户注册成功事件。
 *
 * <p>
 * 只带下游需要的字段，**不带整个 {@link User} 实体**：实体里有密码哈希，
 * 事件会被序列化进消息队列、写进日志，带上它等于把哈希散出去。
 *
 * @author klausxie
 * @since 2023/8/16
 */
@Data
public class UserCreatedEvent {

    private Long userId;

    private String mobile;

    private Integer age;

    /**
     * 注册赠送的积分
     */
    private int points;

}
