package cn.mklaus.app.application.user.query.response;

import cn.mklaus.app.domain.user.User;
import lombok.Builder;
import lombok.Data;

/**
 * 用户信息响应模型。刻意不含 password 字段：领域模型不允许直接当响应体返回。
 *
 * @author klaus
 * @since 2026/9/28
 */
@Data
@Builder
public class UserInfo {

    private Long id;
    private String mobile;
    private String nickname;
    private String avatar;
    private Integer age;

    public static UserInfo of(User user) {
        return UserInfo.builder()
            .id(user.getId())
            .mobile(user.getMobile().value())
            .nickname(user.getNickname())
            .avatar(user.getAvatar())
            .age(user.getAge())
            .build();
    }

}
