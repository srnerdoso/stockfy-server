package br.com.threadstech.stockfy.modules.users.domain;

import br.com.threadstech.stockfy.modules.users.domain.model.UserRole;
import br.com.threadstech.stockfy.modules.users.domain.model.UserStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.Arrays;

class UserEnumsTest {

    @Test
    @DisplayName("Should verify UserRole has expected values")
    void userRole_shouldHaveExpectedValues() {
        assertTrue(Arrays.stream(UserRole.values())
                .anyMatch(role -> role.name().equals("ADMIN")));
        assertTrue(Arrays.stream(UserRole.values())
                .anyMatch(role -> role.name().equals("USER")));
    }

    @Test
    @DisplayName("Should verify UserStatus has expected values")
    void userStatus_shouldHaveExpectedValues() {
        assertTrue(Arrays.stream(UserStatus.values())
                .anyMatch(status -> status.name().equals("ACTIVE")));
        assertTrue(Arrays.stream(UserStatus.values())
                .anyMatch(status -> status.name().equals("LOCKED")));
    }
}
