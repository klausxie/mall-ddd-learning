package cn.mklaus.app.application.user.command.assembler;

import cn.mklaus.app.application.user.command.request.AddressCreateRequest;
import cn.mklaus.app.application.user.command.request.UserCreateRequest;
import cn.mklaus.app.domain.common.PasswordHasher;
import cn.mklaus.app.domain.user.Address;
import cn.mklaus.app.domain.user.Mobile;
import cn.mklaus.app.domain.user.User;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 请求对象 → 领域对象。校验交给 domain（{@code UserValidator} / 实体方法），这里只做转换。
 *
 * @author klausxie
 * @since 2023/8/16
 */
@Component
@AllArgsConstructor
public class UserAssembler {

    private final PasswordHasher passwordHasher;

    public User buildUser(UserCreateRequest req) {
        User user = new User();
        user.setMobile(new Mobile(req.getMobile()));
        // 只存哈希，明文密码不落库也不进日志
        user.setPassword(passwordHasher.encode(req.getPassword()));
        user.setAge(req.getAge());

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
