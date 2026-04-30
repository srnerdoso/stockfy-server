package br.com.threadstech.stockfy.modules.users.domain.exception;

public class InvalidUserRolesException extends RuntimeException {
  public InvalidUserRolesException() {
    super("exception.user-roles-invalid");
  }
}
