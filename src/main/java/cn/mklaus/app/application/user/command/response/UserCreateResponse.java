package cn.mklaus.app.application.user.command.response;

import cn.mklaus.app.application.user.query.response.UserInfo;
import cn.mklaus.app.domain.user.User;
import lombok.Builder;
import lombok.Data;

/**
 * 注册结果响应模型：用户信息 + 本次赠送的积分。
 *
 * <p>
 * 把积分回显出来，是为了让"注册送积分"这条链路能直接从接口响应里看到，
 * 而不必去翻事件日志。
 *
 * @author klaus
 * @since 2026/9/28
 */
@Data
@Builder
public class UserCreateResponse {

    private UserInfo user;

    /**
     * 注册赠送的积分
     */
    private int points;

    public static UserCreateResponse of(User user, int points) {
        return UserCreateResponse.builder()
            .user(UserInfo.of(user))
            .points(points)
            .build();
    }

}
