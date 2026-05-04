package br.com.threadstech.stockfy.users.application.exception;

public class EmailAlreadyExistsException extends RuntimeException {

	public EmailAlreadyExistsException() {
		super("exception.email-already-exists");
	}

}
