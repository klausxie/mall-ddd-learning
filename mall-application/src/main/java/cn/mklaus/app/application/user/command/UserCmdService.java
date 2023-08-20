package cn.mklaus.app.application.user.command;

import cn.mklaus.app.application.user.command.request.AddressCreateRequest;
import cn.mklaus.app.application.user.command.request.AddressRemoveRequest;
import cn.mklaus.app.application.user.command.request.AddressUpdateRequest;
import cn.mklaus.app.application.user.command.request.UserCreateRequest;
import cn.mklaus.app.application.user.model.AddressDTO;
import cn.mklaus.app.application.user.model.UserDTO;
import cn.mklaus.app.common.auth.Context;
import cn.mklaus.app.domain.common.CaptchaService;
import cn.mklaus.app.domain.user.Address;
import cn.mklaus.app.domain.user.AddressRepository;
import cn.mklaus.app.domain.user.User;
import cn.mklaus.app.domain.user.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * @author klausxie
 * @since 2023/8/16
 */
@Component
@AllArgsConstructor
public class UserCmdService {

    private final UserAssembler userAssembler;
    private final CaptchaService captchaService;
    private final UserRepository userRepository;
    private final AddressRepository addressRepository;

    public UserDTO createUser(UserCreateRequest req) {
        boolean captchaValidate = captchaService.isCaptchaValidate(req.getMobile(), req.getCaptcha());
        Assert.state(captchaValidate, "验证码不正确");

        User user = userAssembler.buildUser(req);
        userRepository.saveUser(user);
        return userAssembler.toUserDTO(user);
    }

    public AddressDTO createAddress(AddressCreateRequest req) {
        Address address = userAssembler.buildAddress(req);
        address.setUserId(Context.currentOperator().getId());
        addressRepository.saveAddress(address);
        return userAssembler.toAddressDTO(address);
    }

    public void updateAddress(AddressUpdateRequest req) {
        Address address = addressRepository.getAddress(req.getAddressId())
                .orElseThrow(() -> new RuntimeException("地址不存在"));
        Assert.state(address.getUserId().equals(Context.currentOperator().getId()), "没有权限");

        BeanUtils.copyProperties(req, address);
        address.setId(req.getAddressId());
        address.validate();

        addressRepository.updateAddress(address);
    }

    public void removeAddress(AddressRemoveRequest req) {
        Address address = addressRepository.getAddress(req.getAddressId())
                .orElseThrow(() -> new RuntimeException("地址不存在"));
        Assert.state(address.getUserId().equals(Context.currentOperator().getId()), "没有权限");
        addressRepository.removeAddress(address);
    }

}
