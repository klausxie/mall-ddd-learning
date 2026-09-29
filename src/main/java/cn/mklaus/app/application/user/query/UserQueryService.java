package cn.mklaus.app.application.user.query;

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
 * 用户读操作：只做编排（取数 → 转响应模型），不写业务规则。
 *
 * <p>
 * 这里**不拆接口/实现**：只有一个实现时那层接口是纯仪式，还会让每个新功能多写两个文件。
 *
 * @author klausxie
 * @since 2023/11/4
 */
@Service
@AllArgsConstructor
public class UserQueryService {

    private final UserMapper userMapper;
    private final AddressMapper addressMapper;

    public UserInfo getCurrentUser() {
        Operator operator = Context.currentOperator();
        User user = userMapper.getUser(operator.getId())
            .orElseThrow(() -> new ErrorCodeException(UserErrorCode.USER_NOT_EXISTS));
        return UserInfo.of(user);
    }

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
