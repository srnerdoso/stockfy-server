package br.com.threadstech.stockfy.modules.users.presentation.mapper;

import br.com.threadstech.stockfy.modules.users.application.dto.UserResponse;
import br.com.threadstech.stockfy.modules.users.domain.model.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class UserResponseMapperFactory {

  public UserResponse toResponse(User user) {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    boolean isAdmin =
        auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

    UUID currentUserId = (UUID) auth.getPrincipal();
    boolean isOwner = user.getId().equals(currentUserId);

    if (isAdmin) {
      return mapToFullResponse(user);
    } else if (isOwner) {
      return mapToOwnerResponse(user);
    } else {
      return mapToPublicResponse(user);
    }
  }

  private UserResponse mapToFullResponse(User user) {
    return UserResponse.builder()
        .id(user.getId())
        .name(user.getName())
        .email(user.getEmail().value())
        .role(user.getRole())
        .status(user.getStatus())
        .active(user.isActive())
        // Fields like audit would need to be passed or fetched if Envers is used
        .build();
  }

  private UserResponse mapToOwnerResponse(User user) {
    return UserResponse.builder()
        .id(user.getId())
        .name(user.getName())
        .email(user.getEmail().value())
        .role(user.getRole())
        .build();
  }

  private UserResponse mapToPublicResponse(User user) {
    return UserResponse.builder().id(user.getId()).name(user.getName()).build();
  }
}
