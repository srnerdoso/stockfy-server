package br.com.threadstech.stockfy.validation;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class AddressCreateDto {

  @NotEmpty(message = "{NotEmpty.addressDto.street}")
  @Size(min = 2, max = 255, message = "{Size.addressDto.street}")
  private String street;

  @NotEmpty(message = "{NotEmpty.addressDto.number}")
  @Size(min = 1, max = 20, message = "{Size.addressDto.number}")
  private String number;

  @NotEmpty(message = "{NotEmpty.addressDto.complement}")
  @Size(min = 2, max = 255, message = "{Size.addressDto.complement}")
  private String complement;

  @NotEmpty(message = "{NotEmpty.addressDto.neighborhood}")
  @Size(min = 2, max = 255, message = "{Size.addressDto.neighborhood}")
  private String neighborhood;

  @NotEmpty(message = "{NotEmpty.addressDto.city}")
  @Size(min = 2, max = 255, message = "{NotEmpty.addressDto.city}")
  private String city;

  @NotEmpty(message = "{NotEmpty.addressDto.state}")
  @Size(min = 2, max = 65, message = "{Size.addressDto.state}")
  private String state;

  @NotEmpty(message = "{NotEmpty.addressDto.zipCode}")
  @Size(min = 2, max = 20, message = "{Size.addressDto.zipCode}")
  private String zipCode;

  @NotEmpty(message = "{NotEmpty.addressDto.country}")
  @Size(min = 2, max = 100, message = "{Size.addressDto.country}")
  private String country;
}
