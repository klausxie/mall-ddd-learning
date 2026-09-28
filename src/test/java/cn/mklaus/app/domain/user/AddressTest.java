package cn.mklaus.app.domain.user;

import cn.mklaus.app.common.exception.ErrorCodeException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author klaus
 * @since 2026/9/28
 */
class AddressTest {

    @Test
    void shouldRejectBlankRecipient() {
        assertCode(UserErrorCode.RECIPIENT_IS_REQUIRED, address(null)::validate);
        assertCode(UserErrorCode.RECIPIENT_IS_REQUIRED, address("")::validate);
        assertCode(UserErrorCode.RECIPIENT_IS_REQUIRED, address("   ")::validate);
    }

    @Test
    void shouldPassWhenRecipientPresent() {
        assertDoesNotThrow(() -> address("张三").validate());
    }

    @Test
    void shouldOnlyAllowOwnerToModify() {
        Address address = address("张三");
        address.setUserId(7L);

        assertDoesNotThrow(() -> address.assertOwnedBy(7L));
        assertCode(UserErrorCode.NO_PERMISSION, () -> address.assertOwnedBy(8L));
        assertCode(UserErrorCode.NO_PERMISSION, () -> address.assertOwnedBy(null));
    }

    @Test
    void shouldRejectOwnerCheckWhenAddressHasNoOwner() {
        Address address = address("张三");

        assertCode(UserErrorCode.NO_PERMISSION, () -> address.assertOwnedBy(7L));
    }

    private static void assertCode(UserErrorCode expected, Runnable action) {
        ErrorCodeException exception = assertThrows(ErrorCodeException.class, action::run);
        assertEquals(expected, exception.getErrorCode());
    }

    private static Address address(String recipient) {
        return Address.builder().recipient(recipient).build();
    }

}
