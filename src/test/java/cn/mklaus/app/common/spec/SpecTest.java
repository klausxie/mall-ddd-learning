package cn.mklaus.app.common.spec;

import cn.mklaus.app.common.exception.ErrorCode;
import cn.mklaus.app.common.exception.Violation;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author klaus
 * @since 2026/9/28
 */
class SpecTest {

    private static final Spec<Integer> AT_LEAST_18 = value -> value >= 18
        ? Optional.empty()
        : Optional.of(Violation.of(TestErrorCode.BELOW_MIN, value));

    private static final Spec<Integer> AT_MOST_60 = value -> value <= 60
        ? Optional.empty()
        : Optional.of(Violation.of(TestErrorCode.ABOVE_MAX, value));

    private static final Spec<Integer> EVEN = value -> value % 2 == 0
        ? Optional.empty()
        : Optional.of(Violation.of(TestErrorCode.ODD, value));

    @Test
    void shouldBeSatisfiedWhenViolationIsEmpty() {
        assertTrue(AT_LEAST_18.isSatisfiedBy(18));
        assertTrue(AT_LEAST_18.violation(18).isEmpty());
    }

    @Test
    void shouldOwnViolationReasonWhenNotSatisfied() {
        Violation violation = AT_LEAST_18.violation(17).orElseThrow();

        assertFalse(AT_LEAST_18.isSatisfiedBy(17));
        assertEquals(TestErrorCode.BELOW_MIN, violation.getErrorCode());
        assertEquals("值 17 小于下限 18", violation.getMessage());
    }

    @Test
    void andShouldRequireBothSidesAndReturnFirstViolation() {
        Spec<Integer> range = AT_LEAST_18.and(AT_MOST_60);

        assertTrue(range.isSatisfiedBy(30));
        assertEquals(TestErrorCode.BELOW_MIN, range.violation(17).orElseThrow().getErrorCode());
        assertEquals(TestErrorCode.ABOVE_MAX, range.violation(70).orElseThrow().getErrorCode());
    }

    @Test
    void orShouldPassWhenEitherSideSatisfied() {
        Spec<Integer> spec = AT_LEAST_18.or(EVEN);

        assertTrue(spec.isSatisfiedBy(19));
        assertTrue(spec.isSatisfiedBy(20));
    }

    @Test
    void orShouldReturnFirstViolationWhenBothViolated() {
        Spec<Integer> spec = AT_LEAST_18.or(EVEN);

        assertEquals(TestErrorCode.BELOW_MIN, spec.violation(17).orElseThrow().getErrorCode());
    }

    @Test
    void notShouldReportGivenReasonWhenInnerSatisfied() {
        Spec<Integer> underage = AT_LEAST_18.not(TestErrorCode.INNER_SATISFIED);

        assertTrue(underage.isSatisfiedBy(17));
        assertEquals(TestErrorCode.INNER_SATISFIED, underage.violation(18).orElseThrow().getErrorCode());
    }

    @Getter
    @AllArgsConstructor
    private enum TestErrorCode implements ErrorCode {

        BELOW_MIN(1, "值 {0} 小于下限 18"),
        ABOVE_MAX(2, "值 {0} 大于上限 60"),
        ODD(3, "值 {0} 不是偶数"),
        INNER_SATISFIED(4, "值 {0} 不应满足内层约束");

        private final int code;
        private final String template;

    }

}
