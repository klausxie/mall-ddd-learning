package cn.mklaus.app.domain.user;

import lombok.Data;

/**
 * 用户实体。
 *
 * <p>
 * 这里的访问器是 Lombok {@code @Data} 生成的：**这是有意保留的取舍**，见 ARCHITECTURE.md 的"已知偏差"。
 * 规则不靠这一层守，而靠值对象（{@link Mobile}）、{@link UserValidator}、
 * {@code UserMustBeAdultSpec} 与 {@code RegistrationPointsPolicy}。
 *
 * @author klausxie
 * @since 2023/8/15
 */
@Data
public class User {

    private Long id;
    private Mobile mobile;
    private String password;
    private String nickname;
    private String avatar;
    private Integer age;

    /**
     * 注册成功事件。**事件内容由领域决定**，编排层只决定"什么时候发"。
     *
     * <p>
     * id 是入库回填后才有的，所以这个方法必须在 {@code saveUser} 之后调用。
     *
     * @param points 本次赠送的积分（由 {@code RegistrationPointsPolicy} 算出）
     */
    public UserCreatedEvent registeredEvent(int points) {
        UserCreatedEvent event = new UserCreatedEvent();
        event.setUserId(id);
        event.setMobile(mobile.value());
        event.setAge(age);
        event.setPoints(points);
        return event;
    }

}
