package cn.mklaus.app.application.user.query;

import cn.mklaus.app.application.user.model.AddressDTO;
import cn.mklaus.app.application.user.model.UserDTO;
import cn.mklaus.app.application.user.query.request.AddressPageRequest;
import cn.mklaus.app.common.auth.Context;
import cn.mklaus.app.common.auth.Operator;
import cn.mklaus.app.common.model.Page;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * @author klausxie
 * @since 2023/8/16
 */
@Component
@AllArgsConstructor
public class UserQueryService {

    private final UserQuery userQuery;
    private final AddressQuery addressQuery;

    public UserDTO getCurrentUser() {
        Operator operator = Context.currentOperator();
        return userQuery.getUser(operator.getId()).orElseThrow(() -> new RuntimeException("用户不存在"));
    }

    public Page<AddressDTO> pageAddress(AddressPageRequest req) {
        Operator operator = Context.currentOperator();
        return addressQuery.pageAddress(operator.getId(), req);
    }

}
