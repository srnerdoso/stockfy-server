package br.com.threadstech.stockfy.users.domain.model;

import br.com.threadstech.stockfy.users.application.exception.InvalidPasswordException;

public record Password(String value) {
	public Password {
		if (value == null || value.isBlank() || value.length() < 8) {
			throw new InvalidPasswordException();
		}
	}
}
