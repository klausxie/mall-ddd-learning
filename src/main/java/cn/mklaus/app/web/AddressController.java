package cn.mklaus.app.web;

import cn.mklaus.app.application.user.command.UserCmdService;
import cn.mklaus.app.application.user.command.request.AddressCreateRequest;
import cn.mklaus.app.application.user.command.request.AddressRemoveRequest;
import cn.mklaus.app.application.user.command.request.AddressUpdateRequest;
import cn.mklaus.app.application.user.query.UserQueryService;
import cn.mklaus.app.application.user.query.request.AddressPageRequest;
import cn.mklaus.app.application.user.query.response.AddressInfo;
import cn.mklaus.app.common.model.Page;
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
@RequestMapping("address")
@AllArgsConstructor
public class AddressController {

    private final UserCmdService userCmdService;
    private final UserQueryService userQueryService;

    @PostMapping("create")
    public Response<AddressInfo> createAddress(@Valid @RequestBody AddressCreateRequest request) {
        return Response.ok(userCmdService.createAddress(request));
    }

    @PostMapping("update")
    public Response<Void> updateAddress(@Valid @RequestBody AddressUpdateRequest request) {
        userCmdService.updateAddress(request);
        return Response.ok();
    }

    @PostMapping("remove")
    public Response<Void> removeAddress(@RequestBody AddressRemoveRequest request) {
        userCmdService.removeAddress(request);
        return Response.ok();
    }

    @GetMapping("page")
    public Response<Page<AddressInfo>> pageAddress(AddressPageRequest request) {
        return Response.ok(userQueryService.pageAddress(request));
    }

}
