package cn.mklaus.app.application.user.command;

import cn.mklaus.app.application.user.command.request.AddressCreateRequest;
import cn.mklaus.app.application.user.command.request.UserCreateRequest;
import cn.mklaus.app.domain.user.Address;
import cn.mklaus.app.domain.user.User;
import cn.mklaus.app.domain.user.UserValidator;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * @author klausxie
 * @since 2023/8/16
 */
@Component
@AllArgsConstructor
public class UserAssembler {

    private final UserValidator userValidator;

    public User buildUser(UserCreateRequest req) {
        userValidator.assertMobileCanUse(req.getMobile());
        userValidator.assertPasswordValidate(req.getPassword());

        User user = new User();
        user.setMobile(user.getMobile());
        user.setPassword(user.getPassword());

        return user;
    }

    public Address buildAddress(AddressCreateRequest req) {
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
