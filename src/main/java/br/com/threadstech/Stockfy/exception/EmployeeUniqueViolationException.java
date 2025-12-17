package br.com.threadstech.stockfy.exception;

import lombok.Getter;

@Getter
public class EmployeeUniqueViolationException extends RuntimeException {
  private final String fieldName;

  public EmployeeUniqueViolationException(String fieldNameDisplay) {
    this.fieldName = fieldNameDisplay;
  }
}
