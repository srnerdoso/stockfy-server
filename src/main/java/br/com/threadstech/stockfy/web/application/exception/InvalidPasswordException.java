package br.com.threadstech.stockfy.web.application.exception;

public class InvalidPasswordException extends RuntimeException {
  public InvalidPasswordException() {
    super("password.invalid");
  }
}
