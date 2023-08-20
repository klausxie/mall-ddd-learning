package cn.mklaus.app.domain.user;

import lombok.Data;

/**
 * @author klausxie
 * @since 2023/8/15
 */
@Data
public class User {

    private Long id;
    private String mobile;
    private String password;
    private String nickname;
    private String avatar;

}
