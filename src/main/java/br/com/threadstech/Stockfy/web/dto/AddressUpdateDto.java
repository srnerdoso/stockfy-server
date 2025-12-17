package br.com.threadstech.stockfy.web.dto;

import jakarta.annotation.Nullable;
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
public class AddressUpdateDto {

  @Nullable
  @Size(min = 2, max = 255, message = "{Size.addressDto.street}")
  private String street;

  @Nullable
  @Size(min = 1, max = 20, message = "{Size.addressDto.number}")
  private String number;

  @Nullable
  @Size(min = 2, max = 255, message = "{Size.addressDto.complement}")
  private String complement;

  @Nullable
  @Size(min = 2, max = 255, message = "{Size.addressDto.neighborhood}")
  private String neighborhood;

  @Nullable
  @Size(min = 2, max = 255, message = "{NotEmpty.addressDto.city}")
  private String city;

  @Nullable
  @Size(min = 2, max = 65, message = "{Size.addressDto.state}")
  private String state;

  @Nullable
  @Size(min = 2, max = 20, message = "{Size.addressDto.zipCode}")
  private String zipCode;

  @Nullable
  @Size(min = 2, max = 100, message = "{Size.addressDto.country}")
  private String country;
}
