package cn.mklaus.app.application.user.command;

import cn.mklaus.app.application.user.command.assembler.UserAssembler;
import cn.mklaus.app.application.user.command.request.AddressCreateRequest;
import cn.mklaus.app.application.user.command.request.AddressRemoveRequest;
import cn.mklaus.app.application.user.command.request.AddressUpdateRequest;
import cn.mklaus.app.application.user.command.request.UserCreateRequest;
import cn.mklaus.app.application.user.command.response.UserCreateResponse;
import cn.mklaus.app.application.user.query.response.AddressInfo;
import cn.mklaus.app.common.auth.Context;
import cn.mklaus.app.common.exception.ErrorCodeException;
import cn.mklaus.app.common.spec.Specs;
import cn.mklaus.app.domain.common.EventPublisher;
import cn.mklaus.app.domain.user.Address;
import cn.mklaus.app.domain.user.AddressMapper;
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
 * @author klausxie
 * @since 2023/11/4
 */
@Service
@Transactional
@AllArgsConstructor
public class UserCmdServiceImpl implements UserCmdService {

    private final UserAssembler userAssembler;
    private final UserValidator userValidator;
    private final UserMapper userMapper;
    private final AddressMapper addressMapper;
    private final EventPublisher eventPublisher;

    @Override
    public UserCreateResponse createUser(UserCreateRequest req) {
        userValidator.assertCanRegister(req.getMobile(), req.getPassword(), req.getCaptcha());

        User user = userAssembler.buildUser(req);
        Specs.assertSatisfied(new UserMustBeAdultSpec(), user);
        userMapper.saveUser(user);

        int points = new RegistrationPointsPolicy().pointsFor(user);
        eventPublisher.publishAfterCommit(user.registeredEvent(points));

        return UserCreateResponse.of(user, points);
    }

    @Override
    public AddressInfo createAddress(AddressCreateRequest req) {
        Address address = userAssembler.buildAddress(req);
        address.setUserId(Context.currentOperator().getId());
        addressMapper.saveAddress(address);
        return AddressInfo.of(address);
    }

    @Override
    public void updateAddress(AddressUpdateRequest req) {
        Address address = addressMapper.getAddress(req.getAddressId())
            .orElseThrow(() -> new ErrorCodeException(UserErrorCode.ADDRESS_NOT_EXISTS));
        address.assertOwnedBy(Context.currentOperator().getId());

        BeanUtils.copyProperties(req, address);
        address.setId(req.getAddressId());
        address.validate();

        addressMapper.updateAddress(address);
    }

    @Override
    public void removeAddress(AddressRemoveRequest req) {
        Address address = addressMapper.getAddress(req.getAddressId())
            .orElseThrow(() -> new ErrorCodeException(UserErrorCode.ADDRESS_NOT_EXISTS));
        address.assertOwnedBy(Context.currentOperator().getId());
        addressMapper.removeAddress(address);
    }

}
