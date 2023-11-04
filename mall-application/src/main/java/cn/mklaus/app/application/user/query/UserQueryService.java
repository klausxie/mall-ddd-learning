package cn.mklaus.app.application.user.query;

import cn.mklaus.app.repostiory.user.query.model.AddressDTO;
import cn.mklaus.app.repostiory.user.query.model.UserDTO;
import cn.mklaus.app.application.user.query.request.AddressPageRequest;
import cn.mklaus.app.common.model.Page;

/**
 * @author klausxie
 * @since 2023/8/16
 */
public interface UserQueryService {

    UserDTO getCurrentUser();

    Page<AddressDTO> pageAddress(AddressPageRequest req);

}
