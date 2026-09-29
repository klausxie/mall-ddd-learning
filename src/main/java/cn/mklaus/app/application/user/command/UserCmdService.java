package cn.mklaus.app.application.user.command;

import cn.mklaus.app.application.user.query.AddressInfo;
import cn.mklaus.app.common.auth.Context;
import cn.mklaus.app.common.exception.ErrorCodeException;
import cn.mklaus.app.common.spec.Specs;
import cn.mklaus.app.domain.common.EventPublisher;
import cn.mklaus.app.domain.common.PasswordHasher;
import cn.mklaus.app.domain.user.Address;
import cn.mklaus.app.domain.user.AddressMapper;
import cn.mklaus.app.domain.user.Mobile;
import cn.mklaus.app.domain.user.User;
import cn.mklaus.app.domain.user.UserErrorCode;
import cn.mklaus.app.domain.user.UserMapper;
import cn.mklaus.app.domain.user.UserValidator;
import cn.mklaus.app.domain.user.points.RegistrationPointsPolicy;
import cn.mklaus.app.domain.user.spec.UserMustBeAdultSpec;
import lombok.AllArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 用户写操作：只做编排（装配 → 调领域规则 → 事务边界），业务规则在 {@code domain}。
 *
 * <p>
 * 两条约定（见 ARCHITECTURE.md §二）：
 *
 * <ul>
 * <li>**不拆接口/实现**：只有一个实现时那层接口是纯仪式，还会让每个新功能多写两个文件；</li>
 * <li>**转换放哪**：请求 → 领域写成本类的私有方法（可能需要 {@code PasswordHasher} 这类容器协作者）；
 * 只依赖领域对象的响应转换写在模型上（{@code XxxInfo.of(entity)}）。</li>
 * </ul>
 *
 * @author klausxie
 * @since 2023/11/4
 */
@Service
@Transactional
@AllArgsConstructor
public class UserCmdService {

    private final PasswordHasher passwordHasher;
    private final UserValidator userValidator;
    private final UserMapper userMapper;
    private final AddressMapper addressMapper;
    private final EventPublisher eventPublisher;

    public UserCreateResponse createUser(UserCreateRequest req) {
        userValidator.assertCanRegister(req.getMobile(), req.getPassword(), req.getCaptcha());

        User user = buildUser(req);
        Specs.assertSatisfied(new UserMustBeAdultSpec(), user);
        userMapper.saveUser(user);

        int points = new RegistrationPointsPolicy().pointsFor(user);
        eventPublisher.publishAfterCommit(user.registeredEvent(points));

        return UserCreateResponse.of(user, points);
    }

    public AddressInfo createAddress(AddressCreateRequest req) {
        Address address = buildAddress(req);
        address.setUserId(Context.currentOperator().getId());
        addressMapper.saveAddress(address);
        return AddressInfo.of(address);
    }

    public void updateAddress(AddressUpdateRequest req) {
        Address address = addressMapper.getAddress(req.getAddressId())
            .orElseThrow(() -> new ErrorCodeException(UserErrorCode.ADDRESS_NOT_EXISTS));
        address.assertOwnedBy(Context.currentOperator().getId());

        BeanUtils.copyProperties(req, address);
        address.setId(req.getAddressId());
        address.validate();

        addressMapper.updateAddress(address);
    }

    public void removeAddress(AddressRemoveRequest req) {
        Address address = addressMapper.getAddress(req.getAddressId())
            .orElseThrow(() -> new ErrorCodeException(UserErrorCode.ADDRESS_NOT_EXISTS));
        address.assertOwnedBy(Context.currentOperator().getId());
        addressMapper.removeAddress(address);
    }

    private User buildUser(UserCreateRequest req) {
        User user = new User();
        user.setMobile(new Mobile(req.getMobile()));
        // 只存哈希，明文密码不落库也不进日志
        user.setPassword(passwordHasher.encode(req.getPassword()));
        user.setAge(req.getAge());
        return user;
    }

    /**
     * 转换后顺手取一次不变量：不成立的实体不该继续往后流。
     */
    private Address buildAddress(AddressCreateRequest req) {
        Address address = Address.builder()
            .province(req.getProvince())
            .city(req.getCity())
            .district(req.getDistrict())
            .detail(req.getDetail())
            .recipient(req.getRecipient())
            .phone(req.getPhone())
            .build();

        address.validate();
        return address;
    }

}
