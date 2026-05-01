package br.com.threadstech.stockfy.modules.users.application.exception;

public class InvalidCredentialsException extends RuntimeException {
  public InvalidCredentialsException() {
    super("exception.invalid-credentials");
  }
}
