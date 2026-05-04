package br.com.threadstech.stockfy.users.domain.model;

import java.util.regex.Pattern;

public record Email(String value) {
  private static final String EMAIL_REGEX = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$";
  private static final Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_REGEX);

  public Email {
    if (value == null || value.isBlank() || !EMAIL_PATTERN.matcher(value).matches()) {
      throw new IllegalArgumentException("Invalid email format");
    }
  }
}
