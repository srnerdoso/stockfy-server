package br.com.threadstech.stockfy.web.dto;

import br.com.threadstech.stockfy.validation.Phone;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ContactCreateDto {

  @Nullable
  @Email(message = "{Email.contactDto.email}")
  private String email;

  @Phone(message = "{Phone.contactDto.phoneNumber}")
  @NotBlank(message = "{NotBlank.contactDto.phoneNumber}")
  private String phoneNumber;
}
