package br.com.threadstech.stockfy.users.application.exception;

public class InvalidPasswordException extends RuntimeException {

	public InvalidPasswordException() {
		super("user.password.invalid");
	}

}
