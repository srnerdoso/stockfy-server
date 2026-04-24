package br.com.threadstech.stockfy.modules.users.application.dto;

import br.com.threadstech.stockfy.modules.users.domain.model.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RegisterUserRequest(
    @NotBlank String name,
    @Email String email,
    @NotBlank String password,
    String confirmPassword,
    @NotNull UserRole role) {}
