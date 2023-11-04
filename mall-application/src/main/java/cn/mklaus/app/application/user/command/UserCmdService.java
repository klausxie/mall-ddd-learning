package cn.mklaus.app.application.user.command;

import cn.mklaus.app.application.user.command.request.AddressCreateRequest;
import cn.mklaus.app.application.user.command.request.AddressRemoveRequest;
import cn.mklaus.app.application.user.command.request.AddressUpdateRequest;
import cn.mklaus.app.application.user.command.request.UserCreateRequest;
import cn.mklaus.app.repostiory.user.query.model.AddressDTO;
import cn.mklaus.app.repostiory.user.query.model.UserDTO;

/**
 * @author klausxie
 * @since 2023/8/16
 */
public interface UserCmdService {
    UserDTO createUser(UserCreateRequest req);

    AddressDTO createAddress(AddressCreateRequest req);

    void updateAddress(AddressUpdateRequest req);

    void removeAddress(AddressRemoveRequest req);

}
