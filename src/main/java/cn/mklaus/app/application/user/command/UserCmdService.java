package cn.mklaus.app.application.user.command;

import cn.mklaus.app.application.user.command.request.AddressCreateRequest;
import cn.mklaus.app.application.user.command.request.AddressRemoveRequest;
import cn.mklaus.app.application.user.command.request.AddressUpdateRequest;
import cn.mklaus.app.application.user.command.request.UserCreateRequest;
import cn.mklaus.app.application.user.command.response.UserCreateResponse;
import cn.mklaus.app.application.user.query.response.AddressInfo;

/**
 * @author klausxie
 * @since 2023/8/16
 */
public interface UserCmdService {

    UserCreateResponse createUser(UserCreateRequest req);

    AddressInfo createAddress(AddressCreateRequest req);

    void updateAddress(AddressUpdateRequest req);

    void removeAddress(AddressRemoveRequest req);

}
