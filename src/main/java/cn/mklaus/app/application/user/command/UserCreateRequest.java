package cn.mklaus.app.application.user.command;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * @author klausxie
 * @since 2023/8/16
 */
@Data
public class UserCreateRequest {

    @NotBlank(message = "手机号不能为空")
    private String mobile;

    @NotBlank(message = "验证码不能为空")
    private String captcha;

    @NotBlank(message = "密码不能为空")
    private String password;

    @NotNull(message = "年龄不能为空")
    private Integer age;

}
