package br.com.threadstech.stockfy.exception;

import lombok.Getter;

@Getter
public class ProductUniqueViolationException extends RuntimeException {
  private final String barCode;

  public ProductUniqueViolationException(String barCode) {
    this.barCode = barCode;
  }
}
