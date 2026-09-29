package cn.mklaus.app.application.user.command;

import cn.mklaus.app.common.auth.TokenCodec;
import cn.mklaus.app.domain.user.User;
import cn.mklaus.app.domain.user.UserValidator;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 登录：校验凭据 → 签发令牌，只做编排。
 *
 * <p>
 * 与 {@link UserCmdService} 同放 {@code command}：登录是"POST 用例"，请求 / 响应模型要与 Service 同包
 * （见 AGENTS.md）。它不写库，所以**没有** {@code @Transactional}；校验规则在 {@link UserValidator}，
 * 令牌怎么签名在 {@code infrastructure/security}，这里只负责把两者串起来。
 *
 * @author klaus
 * @since 2026/9/30
 */
@Service
@AllArgsConstructor
public class UserAuthService {

    private final UserValidator userValidator;
    private final TokenCodec tokenCodec;

    public UserLoginResponse login(UserLoginRequest req) {
        User user = userValidator.authenticate(req.getMobile(), req.getPassword());
        return UserLoginResponse.of(tokenCodec.issue(user.getId()), user);
    }

}
