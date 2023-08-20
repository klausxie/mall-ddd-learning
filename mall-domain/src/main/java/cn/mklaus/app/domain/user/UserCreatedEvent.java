package cn.mklaus.app.domain.user;

import lombok.Data;

/**
 * @author klausxie
 * @since 2023/8/16
 */
@Data
public class UserCreatedEvent {

    private User user;

}
