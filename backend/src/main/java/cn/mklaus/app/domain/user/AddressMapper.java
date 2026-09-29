package cn.mklaus.app.domain.user;

import cn.mklaus.app.domain.user.query.AddressPageCondition;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Optional;

/**
 * @author klausxie
 * @since 2023/8/16
 */
@Mapper
public interface AddressMapper {

    Optional<Address> getAddress(Long addressId);

    void saveAddress(Address address);

    void updateAddress(Address address);

    void removeAddress(Address address);

    /** 分页取记录，分页参数用内部的 offset / size。 */
    List<Address> listAddress(AddressPageCondition cnd);

    /** 与 {@link #listAddress} 用同一套过滤条件统计总数。 */
    long countAddress(AddressPageCondition cnd);

}
