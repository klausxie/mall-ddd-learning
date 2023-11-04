package cn.mklaus.app.repostiory.user.convertor;

import cn.mklaus.app.domain.user.Address;
import cn.mklaus.app.repostiory.user.query.model.AddressDTO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * @author klausxie
 * @since 2023/9/4
 */
@Mapper
public interface AddressConvertor {

    AddressConvertor INSTANCE = Mappers.getMapper(AddressConvertor.class);

    AddressDTO toAddressDTO(Address address);

}
