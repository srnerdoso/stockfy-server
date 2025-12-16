package br.com.threadstech.stockfy.web.dto;

import br.com.threadstech.stockfy.validation.AddressCreateDto;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
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

// TODO: Validar se a data de nascimento é maior que a data atual

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class CustomerCreateDto {

  @NotBlank(message = "{NotBlank.customerDto.fullName}")
  @Size(min = 2, max = 255, message = "{Size.customerDto.fullName}")
  private String fullName;

  @CPF(message = "{CPF.customerDto.cpf}")
  @NotBlank(message = "{NotBlank.customerDto.cpf}")
  private String cpf;

  @JsonFormat(pattern = "yyyy-MM-dd")
  @NotNull(message = "{NotNull.customerDto.birthday}")
  private LocalDate birthday;

  @Valid
  @NotNull(message = "{NotNull.customerDto.contact}")
  private ContactCreateDto contact;

  @Valid
  @NotNull(message = "{NotNull.customerDto.address}")
  private AddressCreateDto address;
}
