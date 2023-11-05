package cn.mklaus.app.application.user.query;

import cn.mklaus.app.application.user.query.request.AddressPageRequest;
import cn.mklaus.app.common.model.Page;
import cn.mklaus.app.domain.user.Address;
import cn.mklaus.app.domain.user.User;

/**
 * @author klausxie
 * @since 2023/8/16
 */
public interface UserQueryService {

    User getCurrentUser();

    Page<Address> pageAddress(AddressPageRequest req);

}
