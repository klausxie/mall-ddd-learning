package cn.mklaus.app.application.user.command.request;

import lombok.Data;

/**
 * @author klausxie
 * @since 2023/8/16
 */
@Data
public class UserCreateRequest {

    private String mobile;
    private String captcha;
    private String password;

}
