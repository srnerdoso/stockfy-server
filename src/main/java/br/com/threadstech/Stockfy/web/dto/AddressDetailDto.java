package br.com.threadstech.stockfy.web.dto;

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
public class AddressDetailDto {
  private Long id;
  private String street;
  private String number;
  private String complement;
  private String neighborhood;
  private String city;
  private String state;
  private String zipCode;
  private String country;
}
