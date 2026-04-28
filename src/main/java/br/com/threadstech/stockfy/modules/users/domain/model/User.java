package br.com.threadstech.stockfy.modules.users.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class User {
  private UUID id;
  private String name;
  private Email email;
  private Password password;
  private UserRole role;

  @Builder.Default private UserStatus status = UserStatus.ACTIVE;

  @Builder.Default private boolean active = true;

  private String resetPasswordCodeHash;
  private LocalDateTime resetPasswordExpiresAt;
  private LocalDateTime createdAt;
  private UUID createdBy;
  private LocalDateTime updatedAt;
  private UUID updatedBy;

  public void lock() {
    this.status = UserStatus.LOCKED;
  }

  public void unlock() {
    this.status = UserStatus.ACTIVE;
  }

  public void deactivate() {
    this.active = false;
  }

  public void activate() {
    this.active = true;
  }
}
