package br.com.threadstech.stockfy.users.application.exception;

public class InvalidPasswordResetCodeException extends RuntimeException {
  public InvalidPasswordResetCodeException() {
    super("exception.password-reset-code-invalid");
  }
}
