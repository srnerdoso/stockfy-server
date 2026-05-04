package br.com.threadstech.stockfy.users.application.exception;

public class InvalidCredentialsException extends RuntimeException {

	public InvalidCredentialsException() {
		super("exception.invalid-credentials");
	}

}
