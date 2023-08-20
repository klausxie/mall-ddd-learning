package cn.mklaus.app.infrastructure.repository.user;

import cn.mklaus.app.domain.user.User;
import cn.mklaus.app.domain.user.UserRepository;
import cn.mklaus.app.infrastructure.repository.user.mapper.UserMapper;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * @author klausxie
 * @since 2023/8/16
 */
@Component
@AllArgsConstructor
public class UserRepositoryImpl implements UserRepository {

    private final UserMapper userMapper;

    @Override
    public Optional<User> getUser(long id) {
        return Optional.empty();
    }

    @Override
    public Optional<User> getUserByMobile(String mobile) {
        return Optional.empty();
    }

    @Override
    public void saveUser(User user) {

    }

    @Override
    public void updateUser(User user) {

    }

}
