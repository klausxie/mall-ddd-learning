package cn.mklaus.app.domain.user;

import org.apache.ibatis.annotations.Mapper;

import java.util.Optional;

/**
 * @author klausxie
 * @since 2023/8/16
 */
@Mapper
public interface UserMapper {

    Optional<User> getUser(long id);

    Optional<User> getUserByMobile(Mobile mobile);

    void saveUser(User user);

    void updateUser(User user);

}
