package cn.mklaus.app.domain.user;

import java.util.Optional;

/**
 * @author klausxie
 * @since 2023/8/16
 */
public interface UserRepository {

    Optional<User> getUser(long id);

    Optional<User> getUserByMobile(String mobile);

    void saveUser(User user);

    void updateUser(User user);

}
