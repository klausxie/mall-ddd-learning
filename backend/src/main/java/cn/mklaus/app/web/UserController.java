package cn.mklaus.app.web;

import cn.mklaus.app.application.user.command.UserAuthService;
import cn.mklaus.app.application.user.command.UserCmdService;
import cn.mklaus.app.application.user.command.UserCreateRequest;
import cn.mklaus.app.application.user.command.UserCreateResponse;
import cn.mklaus.app.application.user.command.UserLoginRequest;
import cn.mklaus.app.application.user.command.UserLoginResponse;
import cn.mklaus.app.application.user.query.UserInfo;
import cn.mklaus.app.application.user.query.UserQueryService;
import cn.mklaus.app.common.model.Response;
import cn.mklaus.app.web.auth.TokenCookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author klausxie
 * @since 2023/11/4
 */
@RestController
@RequestMapping("user")
@AllArgsConstructor
public class UserController {

    private final UserCmdService userCmdService;
    private final UserQueryService userQueryService;
    private final UserAuthService userAuthService;
    private final TokenCookie tokenCookie;

    /**
     * 注册。响应里回显本次赠送的积分。
     */
    @PostMapping("create")
    public Response<UserCreateResponse> createUser(@Valid @RequestBody UserCreateRequest request) {
        return Response.ok(userCmdService.createUser(request));
    }

    /**
     * 登录。同一份令牌给两种客户端：响应体里的 {@code token} 给 App / 开放 API（配
     * {@code Authorization: Bearer}），HttpOnly Cookie 给浏览器（JS 读不到，见 {@link TokenCookie}）。
     */
    @PostMapping("login")
    public Response<UserLoginResponse> login(@Valid @RequestBody UserLoginRequest request,
        HttpServletResponse response) {
        UserLoginResponse result = userAuthService.login(request);
        tokenCookie.write(response, result.token());
        return Response.ok(result);
    }

    /**
     * 退出登录：清掉令牌 Cookie。自签令牌无法在服务端吊销，已泄露的令牌在过期前仍然有效
     * （要"强制下线 / 改密即失效"得换服务端会话，见 README）。
     */
    @PostMapping("logout")
    public Response<Void> logout(HttpServletResponse response) {
        tokenCookie.clear(response);
        return Response.ok();
    }

    @GetMapping("get")
    public Response<UserInfo> getCurrentUser() {
        return Response.ok(userQueryService.getCurrentUser());
    }

}
