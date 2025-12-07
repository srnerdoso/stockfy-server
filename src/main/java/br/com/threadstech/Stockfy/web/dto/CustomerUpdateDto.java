package br.com.threadstech.stockfy.web.dto;

import jakarta.annotation.Nullable;
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
public class CustomerUpdateDto {

  @Nullable
  @Size(min = 2, max = 255, message = "{Size.customerDto.fullName}")
  private String fullName;

  @Nullable private ContactUpdateDto contact;
  @Nullable private AddressUpdateDto address;
}
