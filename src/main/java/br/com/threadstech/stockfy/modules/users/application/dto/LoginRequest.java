package br.com.threadstech.stockfy.modules.users.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record LoginRequest(
    @NotBlank(message = "{user.login.email.not-blank}")
        @Pattern(
            regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$",
            message = "{user.login.email.email}")
        String email,
    @NotBlank(message = "{user.login.password.not-blank}") String password) {}
