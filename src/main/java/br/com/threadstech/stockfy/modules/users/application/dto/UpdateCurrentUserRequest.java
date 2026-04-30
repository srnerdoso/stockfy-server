package br.com.threadstech.stockfy.modules.users.application.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@JsonIgnoreProperties(ignoreUnknown = true)
public record UpdateCurrentUserRequest(
    @Size(max = 255, message = "{user.name.size}")
        @Pattern(regexp = "^[\\p{L}\\p{M}0-9 .'-]+$", message = "{user.name.pattern}")
        String name,
    @Email(message = "{user.email.email}") String email) {}
