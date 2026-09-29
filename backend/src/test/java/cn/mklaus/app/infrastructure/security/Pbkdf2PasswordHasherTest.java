package cn.mklaus.app.infrastructure.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author klaus
 * @since 2026/9/28
 */
class Pbkdf2PasswordHasherTest {

    private final Pbkdf2PasswordHasher hasher = new Pbkdf2PasswordHasher();

    @Test
    void shouldNotStoreRawPassword() {
        String encoded = hasher.encode("Passw0rd!");

        assertNotEquals("Passw0rd!", encoded);
        assertFalse(encoded.contains("Passw0rd!"));
        assertTrue(encoded.startsWith("pbkdf2$"));
    }

    @Test
    void shouldMatchCorrectPassword() {
        String encoded = hasher.encode("Passw0rd!");

        assertTrue(hasher.matches("Passw0rd!", encoded));
    }

    @Test
    void shouldNotMatchWrongPassword() {
        String encoded = hasher.encode("Passw0rd!");

        assertFalse(hasher.matches("passw0rd!", encoded));
        assertFalse(hasher.matches("", encoded));
    }

    @Test
    void shouldProduceDifferentHashForSamePassword() {
        assertNotEquals(hasher.encode("Passw0rd!"), hasher.encode("Passw0rd!"));
    }

    @Test
    void shouldTreatBrokenHashAsNotMatched() {
        assertFalse(hasher.matches("Passw0rd!", "not-a-hash"));
        assertFalse(hasher.matches("Passw0rd!", "pbkdf2$abc$x$y"));
        assertFalse(hasher.matches("Passw0rd!", null));
        assertFalse(hasher.matches(null, "pbkdf2$1$x$y"));
    }

}
