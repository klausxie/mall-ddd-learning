package cn.mklaus.app.repostiory.user.query;

import cn.mklaus.app.repostiory.user.query.model.UserDTO;

import java.util.Optional;

/**
 * @author klausxie
 * @since 2023/8/20
 */
public interface UserQuery {

    Optional<UserDTO> getUser(long userId);

}
