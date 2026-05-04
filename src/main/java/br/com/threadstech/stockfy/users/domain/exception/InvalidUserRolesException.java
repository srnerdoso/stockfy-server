package br.com.threadstech.stockfy.users.domain.exception;

public class InvalidUserRolesException extends RuntimeException {
  public InvalidUserRolesException() {
    super("exception.user-roles-invalid");
  }
}
