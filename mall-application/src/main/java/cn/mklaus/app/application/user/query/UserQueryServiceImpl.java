package cn.mklaus.app.application.user.query;

import cn.mklaus.app.application.user.query.request.AddressPageRequest;
import cn.mklaus.app.common.auth.Context;
import cn.mklaus.app.common.auth.Operator;
import cn.mklaus.app.common.model.Page;
import cn.mklaus.app.repostiory.user.query.AddressQuery;
import cn.mklaus.app.repostiory.user.query.UserQuery;
import cn.mklaus.app.repostiory.user.query.model.AddressDTO;
import cn.mklaus.app.repostiory.user.query.model.UserDTO;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * @author klausxie
 * @since 2023/11/4
 */
@Component
@AllArgsConstructor
public class UserQueryServiceImpl implements UserQueryService {

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

