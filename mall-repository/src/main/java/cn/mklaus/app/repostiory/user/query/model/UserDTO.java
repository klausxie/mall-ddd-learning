package cn.mklaus.app.repostiory.user.query.model;

import lombok.Data;

/**
 * @author klausxie
 * @since 2023/8/20
 */
@Data
public class UserDTO {

    private Long id;
    private String mobile;
    private String nickname;
    private String avatar;

}
