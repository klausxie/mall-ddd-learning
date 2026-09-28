package cn.mklaus.app.domain.user.spec;

import cn.mklaus.app.common.exception.Violation;
import cn.mklaus.app.domain.user.User;
import cn.mklaus.app.domain.user.UserErrorCode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author klaus
 * @since 2026/9/28
 */
class UserMustBeAdultSpecTest {

    private final UserMustBeAdultSpec spec = new UserMustBeAdultSpec();

    @Test
    void shouldSatisfyWhenAgeReachesAdultAge() {
        assertTrue(spec.isSatisfiedBy(userOfAge(UserMustBeAdultSpec.ADULT_AGE)));
    }

    @Test
    void shouldViolateWhenAgeBelowAdultAge() {
        Violation violation = spec.violation(userOfAge(UserMustBeAdultSpec.ADULT_AGE - 1)).orElseThrow();

        assertFalse(spec.isSatisfiedBy(userOfAge(UserMustBeAdultSpec.ADULT_AGE - 1)));
        assertEquals(UserErrorCode.USER_MUST_BE_ADULT, violation.getErrorCode());
        assertEquals("必须年满 18 周岁", violation.getMessage());
    }

    @Test
    void shouldViolateWhenAgeMissing() {
        assertFalse(spec.isSatisfiedBy(userOfAge(null)));
        assertTrue(spec.violation(userOfAge(null)).isPresent());
    }

    private static User userOfAge(Integer age) {
        User user = new User();
        user.setAge(age);
        return user;
    }

}
