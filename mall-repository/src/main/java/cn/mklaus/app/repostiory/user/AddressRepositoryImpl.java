package cn.mklaus.app.repostiory.user;

import cn.mklaus.app.domain.user.Address;
import cn.mklaus.app.domain.user.AddressRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * @author klausxie
 * @since 2023/8/20
 */
@Component
public class AddressRepositoryImpl implements AddressRepository{

    @Override
    public Optional<Address> getAddress(Long addressId) {
        return Optional.empty();
    }

    @Override
    public void saveAddress(Address address) {

    }

    @Override
    public void updateAddress(Address address) {

    }

    @Override
    public void removeAddress(Address address) {

    }
}
