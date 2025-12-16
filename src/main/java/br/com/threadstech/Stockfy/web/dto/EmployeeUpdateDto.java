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
public class EmployeeUpdateDto {

  @Nullable
  @Size(min = 2, max = 255, message = "{Size.employeeDto.fullName}")
  private String fullName;

  @Nullable private ContactUpdateDto contact;
  @Nullable private AddressUpdateDto address;
  @Nullable private String role;
}
