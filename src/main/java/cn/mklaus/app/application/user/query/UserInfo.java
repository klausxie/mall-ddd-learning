package cn.mklaus.app.application.user.query;

import cn.mklaus.app.domain.user.User;

/**
 * 用户信息响应模型（record）。刻意不含 password：领域模型不允许直接当响应体返回。
 *
 * @author klaus
 * @since 2026/9/28
 */
public record UserInfo(Long id, String mobile, String nickname, String avatar, Integer age) {

    public static UserInfo of(User user) {
        return new UserInfo(user.getId(), user.getMobile().value(), user.getNickname(), user.getAvatar(),
            user.getAge());
    }

}
