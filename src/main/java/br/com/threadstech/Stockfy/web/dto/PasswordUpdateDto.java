package br.com.threadstech.stockfy.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class PasswordUpdateDto {

  @NotBlank(message = "{NotBlank.password}")
  @Size(min = 8, max = 64, message = "{Size.password}")
  private String currentPassword;

  @NotBlank(message = "{NotBlank.password}")
  @Size(min = 8, max = 64, message = "{Size.password}")
  private String newPassword;

  @NotBlank(message = "{NotBlank.password}")
  @Size(min = 8, max = 64, message = "{Size.password}")
  private String confirmPassword;
}
