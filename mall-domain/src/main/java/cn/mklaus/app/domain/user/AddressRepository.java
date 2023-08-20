package cn.mklaus.app.domain.user;

import cn.mklaus.app.common.model.Page;
import cn.mklaus.app.common.model.Pageable;

import java.util.Optional;

/**
 * @author klausxie
 * @since 2023/8/16
 */
public interface AddressRepository {

    Optional<Address> getAddress(Long addressId);

    void saveAddress(Address address);

    void updateAddress(Address address);

    void removeAddress(Address address);

}
