package br.com.threadstech.stockfy.modules.users.domain.event;

import static org.junit.jupiter.api.Assertions.assertFalse;

import java.io.Serializable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class AccountLockedEventTest {

  @Test
  @DisplayName("Nao deve depender de serializacao Java")
  void accountLockedEvent_whenCreated_thenDoesNotImplementSerializable() {
    assertFalse(Serializable.class.isAssignableFrom(AccountLockedEvent.class));
  }
}
