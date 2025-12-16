package br.com.threadstech.stockfy.web.dto;

import br.com.threadstech.stockfy.validation.Phone;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Email;
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
public class ContactUpdateDto {

  @Nullable
  @Email(message = "{Email.contactDto.email}")
  private String email;

  @Nullable
  @Phone(message = "{Phone.contactDto.phoneNumber}")
  private String phoneNumber;
}
