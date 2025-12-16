package br.com.threadstech.stockfy.web.dto;

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
  private String currentPassword;
  private String newPassword;
  private String confirmPassword;
}
