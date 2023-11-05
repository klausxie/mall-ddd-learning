package cn.mklaus.app.application.user.command;

import cn.mklaus.app.application.user.command.request.AddressCreateRequest;
import cn.mklaus.app.application.user.command.request.AddressRemoveRequest;
import cn.mklaus.app.application.user.command.request.AddressUpdateRequest;
import cn.mklaus.app.application.user.command.request.UserCreateRequest;
import cn.mklaus.app.domain.user.Address;
import cn.mklaus.app.domain.user.User;

/**
 * @author klausxie
 * @since 2023/8/16
 */
public interface UserCmdService {
    User createUser(UserCreateRequest req);

    Address createAddress(AddressCreateRequest req);

    void updateAddress(AddressUpdateRequest req);

    void removeAddress(AddressRemoveRequest req);

}
