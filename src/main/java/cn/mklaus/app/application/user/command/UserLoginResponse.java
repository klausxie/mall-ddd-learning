package cn.mklaus.app.application.user.command;

import cn.mklaus.app.application.user.query.UserInfo;
import cn.mklaus.app.domain.user.User;

/**
 * 登录结果：令牌 + 当前用户信息。
 *
 * <p>
 * 连用户信息一起返回，客户端登录后不必再为"我是谁"多打一次接口；
 * 令牌怎么用见 README 的接口约定（{@code Authorization: Bearer <token>}）。
 *
 * @author klaus
 * @since 2026/9/30
 */
public record UserLoginResponse(String token, UserInfo user) {

    public static UserLoginResponse of(String token, User user) {
        return new UserLoginResponse(token, UserInfo.of(user));
    }

}
