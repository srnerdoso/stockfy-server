package br.com.threadstech.stockfy.exception;

import lombok.Getter;

@Getter
public class CustomerUniqueViolationException extends RuntimeException {
  private final String fieldName;

  public CustomerUniqueViolationException(String fieldNameDisplay) {
    this.fieldName = fieldNameDisplay;
  }
}
