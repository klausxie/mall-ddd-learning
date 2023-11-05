package cn.mklaus.app.application.user.query;

import cn.mklaus.app.application.user.query.request.AddressPageRequest;
import cn.mklaus.app.common.auth.Context;
import cn.mklaus.app.common.auth.Operator;
import cn.mklaus.app.common.model.Page;
import cn.mklaus.app.domain.user.Address;
import cn.mklaus.app.domain.user.AddressRepository;
import cn.mklaus.app.domain.user.User;
import cn.mklaus.app.domain.user.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * @author klausxie
 * @since 2023/11/4
 */
@Component
@AllArgsConstructor
public class UserQueryServiceImpl implements UserQueryService {

    private final UserRepository userRepository;
    private final AddressRepository addressRepository;

    public User getCurrentUser() {
        Operator operator = Context.currentOperator();
        return userRepository.getUser(operator.getId()).orElseThrow(() -> new RuntimeException("用户不存在"));
    }

    public Page<Address> pageAddress(AddressPageRequest req) {
        Operator operator = Context.currentOperator();
        return addressRepository.pageAddress(operator.getId(), req);
    }

}

