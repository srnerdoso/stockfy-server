package br.com.threadstech.stockfy.users.application.exception;

public class CurrentPasswordInvalidException extends RuntimeException {

	public CurrentPasswordInvalidException() {
		super("exception.current-password-invalid");
	}

}
