package br.com.threadstech.stockfy.users.application.dto;

import br.com.threadstech.stockfy.users.domain.model.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record RegisterUserRequest(
    @NotBlank(message = "{user.name.not-blank}") @Pattern(regexp = "^[\\p{L}\\p{M}0-9 .'-]+$", message = "{user.name.pattern}") String name,
    @NotBlank(message = "{user.email.not-blank}") @Email(message = "{user.email.email}") String email,
    @NotBlank(message = "{user.password.not-blank}") String password,
    String confirmPassword,
    @NotNull(message = "{user.role.not-null}") UserRole role) {}
