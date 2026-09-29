package cn.mklaus.app.common.spec;

import cn.mklaus.app.common.exception.CommonErrorCode;
import cn.mklaus.app.common.exception.ErrorCodeException;
import cn.mklaus.app.common.exception.Violation;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author klaus
 * @since 2026/9/28
 */
class SpecsTest {

    private static final Spec<String> NOT_BLANK = value -> value == null || value.isEmpty()
        ? Optional.of(Violation.of(CommonErrorCode.PARAMETER_IS_REQUIRED, "mobile"))
        : Optional.empty();

    @Test
    void shouldNotThrowWhenSpecSatisfied() {
        assertDoesNotThrow(() -> Specs.assertSatisfied(NOT_BLANK, "13800000000"));
    }

    @Test
    void shouldThrowErrorCodeExceptionCarryingSpecReason() {
        ErrorCodeException exception = assertThrows(ErrorCodeException.class,
            () -> Specs.assertSatisfied(NOT_BLANK, ""));

        assertEquals(CommonErrorCode.PARAMETER_IS_REQUIRED, exception.getErrorCode());
        assertEquals("mobile is required", exception.getMessage());
    }

}
