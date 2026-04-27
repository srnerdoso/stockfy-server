package br.com.threadstech.stockfy.modules.users.application.dto;

import br.com.threadstech.stockfy.modules.users.domain.model.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record RegisterUserRequest(
    @NotBlank @Pattern(regexp = "^[\\p{L}\\p{M}0-9 .'-]+$") String name,
    @NotBlank @Email String email,
    @NotBlank String password,
    String confirmPassword,
    @NotNull UserRole role) {}
