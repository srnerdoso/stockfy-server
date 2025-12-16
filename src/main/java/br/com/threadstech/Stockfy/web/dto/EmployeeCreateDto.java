package br.com.threadstech.stockfy.web.dto;

import br.com.threadstech.stockfy.validation.AddressCreateDto;
import br.com.threadstech.stockfy.validation.Role;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.validator.constraints.br.CPF;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeCreateDto {

  @NotBlank(message = "{NotBlank.employeeDto.fullName}")
  @Size(min = 2, max = 255, message = "{Size.employeeDto.fullName}")
  private String fullName;

  @CPF(message = "{CPF.employeeDto.cpf}")
  @NotBlank(message = "{NotBlank.employeeDto.cpf}")
  private String cpf;

  @JsonFormat(pattern = "yyyy-MM-dd")
  @NotNull(message = "{NotNull.employeeDto.birthday}")
  private LocalDate birthday;

  @NotNull(message = "{NotNull.employeeDto.contact}")
  private ContactCreateDto contact;

  @NotNull(message = "{NotNull.employeeDto.address}")
  private AddressCreateDto address;

  @Nullable
  @Role(message = "{Role.employeeDto.role}")
  private String role;

  @NotEmpty(message = "{NotEmpty.employeeDto.password}")
  @Size(min = 8, max = 64, message = "{Size.employeeDto.password}")
  private String password;
}
