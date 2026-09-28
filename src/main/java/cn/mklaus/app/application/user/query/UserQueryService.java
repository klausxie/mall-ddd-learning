package cn.mklaus.app.application.user.query;

import cn.mklaus.app.application.user.query.request.AddressPageRequest;
import cn.mklaus.app.application.user.query.response.AddressInfo;
import cn.mklaus.app.application.user.query.response.UserInfo;
import cn.mklaus.app.common.model.Page;

/**
 * @author klausxie
 * @since 2023/8/16
 */
public interface UserQueryService {

    UserInfo getCurrentUser();

    Page<AddressInfo> pageAddress(AddressPageRequest req);

}
