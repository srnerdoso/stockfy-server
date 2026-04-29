package br.com.threadstech.stockfy.modules.users.application.exception;

public class UserNotFoundException extends RuntimeException {

  public UserNotFoundException() {
    super("exception.user-not-found");
  }
}
