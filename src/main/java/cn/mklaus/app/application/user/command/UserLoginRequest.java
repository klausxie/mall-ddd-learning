package cn.mklaus.app.application.user.command;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 登录请求：手机号即账号（见 {@code db/migration} 里 user 表的唯一键）。
 *
 * @author klaus
 * @since 2026/9/30
 */
@Data
public class UserLoginRequest {

    @NotBlank(message = "手机号不能为空")
    private String mobile;

    @NotBlank(message = "密码不能为空")
    private String password;

}
