package br.com.threadstech.stockfy.modules.users.application.exception;

public class EmailAlreadyExistsException extends RuntimeException {
  public EmailAlreadyExistsException() {
    super("exception.email-already-exists");
  }
}
