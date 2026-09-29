package cn.mklaus.app.domain.user;

import cn.mklaus.app.common.exception.Asserts;

import java.util.regex.Pattern;

/**
 * 手机号值对象：**构造即校验**，领域里不再传裸 {@code String}。
 *
 * <p>
 * 值对象的意义在这里很具体：手机号一旦存在，就一定是合法格式，
 * 后续任何方法都不必再重复校验，也不可能出现"半合法"的手机号在领域里流动。
 *
 * <p>
 * 持久化由 {@code infrastructure.persistence.MobileTypeHandler} 负责，
 * 库里仍然是一列 varchar，领域里是 {@code Mobile}。
 *
 * @param value 11 位手机号
 * @author klaus
 * @since 2026/9/29
 */
public record Mobile(String value) {

    private static final Pattern PATTERN = Pattern.compile("1[3-9]\\d{9}");

    public Mobile {
        Asserts.state(value != null && PATTERN.matcher(value).matches(), UserErrorCode.MOBILE_FORMAT_ILLEGAL);
    }

    /**
     * 便于日志与拼接时直接得到原始号码。
     */
    @Override
    public String toString() {
        return value;
    }

}
