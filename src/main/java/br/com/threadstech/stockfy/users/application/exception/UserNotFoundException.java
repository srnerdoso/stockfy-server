package br.com.threadstech.stockfy.users.application.exception;

public class UserNotFoundException extends RuntimeException {

	public UserNotFoundException() {
		super("exception.user-not-found");
	}

}
