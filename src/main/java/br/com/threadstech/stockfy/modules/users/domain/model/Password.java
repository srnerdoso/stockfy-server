package br.com.threadstech.stockfy.modules.users.domain.model;

public record Password(String value) {
  public Password {
    if (value == null || value.isBlank() || value.length() < 8) {
      throw new IllegalArgumentException("Password must have at least 8 characters");
    }
  }
}
