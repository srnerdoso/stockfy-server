package br.com.threadstech.stockfy.modules.users.application.exception;

public class PasswordMismatchException extends RuntimeException {
  public PasswordMismatchException() {
    super("user.password.mismatch");
  }
}
