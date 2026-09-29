package cn.mklaus.app.web;

import cn.mklaus.app.application.user.command.UserCmdService;
import cn.mklaus.app.application.user.command.UserCreateRequest;
import cn.mklaus.app.application.user.command.UserCreateResponse;
import cn.mklaus.app.application.user.query.UserInfo;
import cn.mklaus.app.application.user.query.UserQueryService;
import cn.mklaus.app.common.model.Response;
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

    /**
     * 注册。响应里回显本次赠送的积分。
     */
    @PostMapping("create")
    public Response<UserCreateResponse> createUser(@Valid @RequestBody UserCreateRequest request) {
        return Response.ok(userCmdService.createUser(request));
    }

    @GetMapping("get")
    public Response<UserInfo> getCurrentUser() {
        return Response.ok(userQueryService.getCurrentUser());
    }

}
