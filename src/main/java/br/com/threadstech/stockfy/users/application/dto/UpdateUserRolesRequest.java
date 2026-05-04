package br.com.threadstech.stockfy.users.application.dto;

import br.com.threadstech.stockfy.users.domain.model.UserRole;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public record UpdateUserRolesRequest(
    List<
            @NotNull(message = "{user.roles.invalid}")
            @Pattern(regexp = "ADMIN|USER", message = "{user.roles.invalid}") String>
        add,
    List<
            @NotNull(message = "{user.roles.invalid}")
            @Pattern(regexp = "ADMIN|USER", message = "{user.roles.invalid}") String>
        remove) {

  public Set<UserRole> rolesToAdd() {
    return toRoles(add);
  }

  public Set<UserRole> rolesToRemove() {
    return toRoles(remove);
  }

  private Set<UserRole> toRoles(List<String> roles) {
    if (roles == null) {
      return Set.of();
    }
    return roles.stream().map(UserRole::valueOf).collect(Collectors.toUnmodifiableSet());
  }
}
