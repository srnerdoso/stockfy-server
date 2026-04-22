package br.com.threadstech.stockfy.modules.users.application.dto;

import br.com.threadstech.stockfy.modules.users.domain.model.UserRole;
import br.com.threadstech.stockfy.modules.users.domain.model.UserStatus;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record UserResponse(
    UUID id,
    String name,
    String email,
    UserRole role,
    UserStatus status,
    boolean active,
    LocalDateTime createdAt,
    UUID createdBy,
    LocalDateTime updatedAt,
    UUID updatedBy
) {
}
