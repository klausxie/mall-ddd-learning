package cn.mklaus.app.common.auth;

import cn.mklaus.app.common.exception.CommonErrorCode;
import cn.mklaus.app.common.exception.ErrorCodeException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author klaus
 * @since 2026/9/28
 */
class ContextTest {

    @AfterEach
    void tearDown() {
        Context.clear();
    }

    @Test
    void shouldReturnOperatorAfterSet() {
        Context.setOperator(new Operator(7L));

        assertEquals(Long.valueOf(7L), Context.currentOperator().getId());
    }

    @Test
    void shouldReportNotLoggedInInsteadOfReturningEmptyOperator() {
        ErrorCodeException exception = assertThrows(ErrorCodeException.class, Context::currentOperator);

        assertEquals(CommonErrorCode.NOT_LOGGED_IN, exception.getErrorCode());
        assertEquals("未登录", exception.getMessage());
    }

    @Test
    void shouldReportNotLoggedInAfterClear() {
        Context.setOperator(new Operator(7L));
        Context.clear();

        assertThrows(ErrorCodeException.class, Context::currentOperator);
    }

}
