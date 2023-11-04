package cn.mklaus.app.repostiory.user.convertor;

import cn.mklaus.app.domain.user.User;
import cn.mklaus.app.repostiory.user.query.model.UserDTO;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

/**
 * @author klausxie
 * @since 2023/9/3
 */
@Mapper
public interface UserConvertor {

    UserConvertor INSTANCE = Mappers.getMapper(UserConvertor.class);

    UserDTO toUserDTO(User user);

    void merge(UserDTO dto, @MappingTarget User user);

}
