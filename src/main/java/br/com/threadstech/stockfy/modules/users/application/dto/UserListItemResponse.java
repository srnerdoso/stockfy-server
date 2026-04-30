package br.com.threadstech.stockfy.modules.users.application.dto;

import br.com.threadstech.stockfy.modules.users.domain.model.User;
import br.com.threadstech.stockfy.modules.users.domain.model.UserRole;
import br.com.threadstech.stockfy.modules.users.domain.model.UserStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record UserListItemResponse(
    String name,
    String email,
    Set<UserRole> roles,
    UserStatus status,
    LocalDateTime createdAt,
    UUID createdBy,
    LocalDateTime updatedAt,
    UUID updatedBy) {

  public static UserListItemResponse from(User user, UserListType type) {
    return switch (type) {
      case SUMMARY -> summary(user);
      case DETAILED -> detailed(user);
    };
  }

  private static UserListItemResponse summary(User user) {
    return new UserListItemResponse(
        user.getName(), user.getEmail().value(), user.getRoles(), null, null, null, null, null);
  }

  private static UserListItemResponse detailed(User user) {
    return new UserListItemResponse(
        user.getName(),
        user.getEmail().value(),
        user.getRoles(),
        user.getStatus(),
        user.getCreatedAt(),
        user.getCreatedBy(),
        user.getUpdatedAt(),
        user.getUpdatedBy());
  }
}
