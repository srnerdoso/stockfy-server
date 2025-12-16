package br.com.threadstech.stockfy.validation;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
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
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class AddressCreateDto {

  @NotBlank(message = "{NotBlank.addressDto.street}")
  @Size(min = 2, max = 255, message = "{Size.addressDto.street}")
  private String street;

  @NotBlank(message = "{NotBlank.addressDto.number}")
  @Size(min = 1, max = 20, message = "{Size.addressDto.number}")
  private String number;

  @NotBlank(message = "{NotBlank.addressDto.complement}")
  @Size(min = 2, max = 255, message = "{Size.addressDto.complement}")
  private String complement;

  @NotBlank(message = "{NotBlank.addressDto.neighborhood}")
  @Size(min = 2, max = 255, message = "{Size.addressDto.neighborhood}")
  private String neighborhood;

  @NotBlank(message = "{NotBlank.addressDto.city}")
  @Size(min = 2, max = 255, message = "{Size.addressDto.city}")
  private String city;

  @NotBlank(message = "{NotBlank.addressDto.state}")
  @Size(min = 2, max = 65, message = "{Size.addressDto.state}")
  private String state;

  @NotBlank(message = "{NotBlank.addressDto.zipCode}")
  @Size(min = 2, max = 20, message = "{Size.addressDto.zipCode}")
  private String zipCode;

  @NotBlank(message = "{NotBlank.addressDto.country}")
  @Size(min = 2, max = 100, message = "{Size.addressDto.country}")
  private String country;
}
