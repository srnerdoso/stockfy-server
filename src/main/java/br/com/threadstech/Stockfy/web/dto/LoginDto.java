package br.com.threadstech.stockfy.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class LoginDto {

  @NotEmpty(message = "{NotEmpty.loginDto.email}")
  @Email(message = "{Email.loginDto.email}")
  private String email;

  @NotEmpty(message = "{NotEmpty.loginDto.password}")
  @Size(min = 8, max = 64, message = "{Size.loginDto.password}")
  private String password;

  @NotEmpty(message = "{NotEmpty.loginDto.deviceId}")
  @Size(min = 16, max = 64, message = "{Size.loginDto.deviceId}")
  private String deviceId;
}
