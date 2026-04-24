package br.com.threadstech.stockfy.modules.users.domain.model;

import br.com.threadstech.stockfy.web.application.exception.InvalidPasswordException;

public record Password(String value) {
  public Password {
    if (value == null || value.isBlank() || value.length() < 8) {
      throw new InvalidPasswordException();
    }
  }
}
