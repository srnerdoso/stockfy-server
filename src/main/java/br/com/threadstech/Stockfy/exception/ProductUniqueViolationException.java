package br.com.threadstech.stockfy.exception;

import lombok.Getter;

@Getter
public class ProductUniqueViolationException extends RuntimeException {
  private final String fieldName;

  public ProductUniqueViolationException(String fieldName) {
    this.fieldName = fieldName;
  }
}
