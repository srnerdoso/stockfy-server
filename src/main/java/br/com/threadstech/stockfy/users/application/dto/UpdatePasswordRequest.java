package br.com.threadstech.stockfy.users.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdatePasswordRequest(
    @Pattern(regexp = "^\\d{6}$", message = "{user.password-reset-code.pattern}") String code,
    String currentPassword,
    @NotBlank(message = "{user.password.not-blank}")
        @Size(min = 8, message = "{user.password.invalid}")
        String newPassword,
    @NotBlank(message = "{user.password.confirmation.not-blank}")
        @Size(min = 8, message = "{user.password.invalid}")
        String confirmPassword) {}
