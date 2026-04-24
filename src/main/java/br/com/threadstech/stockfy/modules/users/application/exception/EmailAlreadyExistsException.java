package br.com.threadstech.stockfy.modules.users.application.exception;

public class EmailAlreadyExistsException extends RuntimeException {
  public EmailAlreadyExistsException() {
    super("email.already.exists");
  }
}
