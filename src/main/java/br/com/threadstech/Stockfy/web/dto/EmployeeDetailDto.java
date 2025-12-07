package br.com.threadstech.stockfy.web.dto;

import br.com.threadstech.stockfy.web.dto.serializer.CpfMaskSerializer;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import tools.jackson.databind.annotation.JsonSerialize;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeDetailDto {
  private Long id;
  private String fullName;

  @JsonSerialize(using = CpfMaskSerializer.class)
  private String cpf;

  private LocalDate birthday;
  private ContactDetailDto contact;
  private AddressDetailDto address;
  private String role;
}
