package cn.mklaus.app.application.user.query;

import cn.mklaus.app.application.user.query.request.AddressPageRequest;
import cn.mklaus.app.application.user.query.response.AddressInfo;
import cn.mklaus.app.application.user.query.response.UserInfo;
import cn.mklaus.app.common.auth.Context;
import cn.mklaus.app.common.auth.Operator;
import cn.mklaus.app.common.exception.ErrorCodeException;
import cn.mklaus.app.common.model.Page;
import cn.mklaus.app.domain.user.AddressMapper;
import cn.mklaus.app.domain.user.User;
import cn.mklaus.app.domain.user.UserErrorCode;
import cn.mklaus.app.domain.user.UserMapper;
import cn.mklaus.app.domain.user.query.AddressPageCondition;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author klausxie
 * @since 2023/11/4
 */
@Service
@AllArgsConstructor
public class UserQueryServiceImpl implements UserQueryService {

    private final UserMapper userMapper;
    private final AddressMapper addressMapper;

    @Override
    public UserInfo getCurrentUser() {
        Operator operator = Context.currentOperator();
        User user = userMapper.getUser(operator.getId())
            .orElseThrow(() -> new ErrorCodeException(UserErrorCode.USER_NOT_EXISTS));
        return UserInfo.of(user);
    }

    @Override
    public Page<AddressInfo> pageAddress(AddressPageRequest req) {
        Operator operator = Context.currentOperator();

        AddressPageCondition cnd = req.buildCondition();
        cnd.setUserId(operator.getId());

        long total = addressMapper.countAddress(cnd);
        List<AddressInfo> records = addressMapper.listAddress(cnd).stream()
            .map(AddressInfo::of)
            .toList();
        return Page.of(req.getCurPage(), req.getPageSize(), total, records);
    }

}
