package cn.mklaus.app.domain.user.spec;

import cn.mklaus.app.common.exception.Violation;
import cn.mklaus.app.common.spec.Spec;
import cn.mklaus.app.domain.user.User;
import cn.mklaus.app.domain.user.UserErrorCode;

import java.util.Optional;

/**
 * 用户必须成年。该约束只在部分用例下成立（注册、下单），因此是规格而不是实体的不变量。
 *
 * @author klaus
 * @since 2026/9/28
 */
public class UserMustBeAdultSpec implements Spec<User> {

    /**
     * 成年年龄
     */
    public static final int ADULT_AGE = 18;

    @Override
    public Optional<Violation> violation(User user) {
        Integer age = user.getAge();
        if (age == null || age < ADULT_AGE) {
            return Optional.of(Violation.of(UserErrorCode.USER_MUST_BE_ADULT, ADULT_AGE));
        }
        return Optional.empty();
    }

}
