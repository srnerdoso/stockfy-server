package br.com.threadstech.stockfy.users.application.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
		@NotBlank(message = "{user.login.email.not-blank}") @Email(message = "{user.login.email.email}") String email,
		@NotBlank(message = "{user.login.password.not-blank}") String password) {
}
