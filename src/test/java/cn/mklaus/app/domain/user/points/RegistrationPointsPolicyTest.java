package cn.mklaus.app.domain.user.points;

import cn.mklaus.app.domain.user.User;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author klaus
 * @since 2026/9/28
 */
class RegistrationPointsPolicyTest {

    private final RegistrationPointsPolicy policy = new RegistrationPointsPolicy();

    @Test
    void shouldAwardJuniorPointsWhenYoungerThanSeniorAge() {
        assertEquals(200, policy.pointsFor(userOfAge(29)));
    }

    @Test
    void shouldAwardSeniorPointsWhenReachingSeniorAge() {
        assertEquals(300, policy.pointsFor(userOfAge(RegistrationPointsPolicy.SENIOR_AGE)));
    }

    @Test
    void shouldAwardSeniorPointsWhenOlderThanSeniorAge() {
        assertEquals(300, policy.pointsFor(userOfAge(45)));
    }

    @Test
    void shouldRejectUserWithoutAge() {
        User user = userOfAge(null);

        assertThrows(IllegalArgumentException.class, () -> policy.pointsFor(user));
    }

    private static User userOfAge(Integer age) {
        User user = new User();
        user.setAge(age);
        return user;
    }

}
