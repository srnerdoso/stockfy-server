package br.com.threadstech.stockfy.exception;

public class UniqueFieldViolationException extends RuntimeException {
  public UniqueFieldViolationException(String message) {
    super(message);
  }
}
