package cn.mklaus.app.common.spec;

import cn.mklaus.app.common.exception.ErrorCode;
import cn.mklaus.app.common.exception.Violation;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link Spec} 的契约：满足时返回 empty，不满足时带着**自己的**错误码与文案。
 *
 * @author klaus
 * @since 2026/9/28
 */
class SpecTest {

    private static final Spec<Integer> AT_LEAST_18 = value -> value >= 18
        ? Optional.empty()
        : Optional.of(Violation.of(TestErrorCode.BELOW_MIN, value));

    @Test
    void shouldBeSatisfiedWhenViolationIsEmpty() {
        assertTrue(AT_LEAST_18.violation(18).isEmpty());
    }

    @Test
    void shouldOwnViolationReasonWhenNotSatisfied() {
        Violation violation = AT_LEAST_18.violation(17).orElseThrow();

        assertEquals(TestErrorCode.BELOW_MIN, violation.getErrorCode());
        assertEquals("值 17 小于下限 18", violation.getMessage());
    }

    @Getter
    @AllArgsConstructor
    private enum TestErrorCode implements ErrorCode {

        BELOW_MIN(1, "值 {0} 小于下限 18");

        private final int code;
        private final String template;

    }

}
