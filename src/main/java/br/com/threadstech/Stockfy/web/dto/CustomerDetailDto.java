package br.com.threadstech.stockfy.web.dto;

import br.com.threadstech.stockfy.entity.CustomerAddress;
import br.com.threadstech.stockfy.entity.CustomerContact;
import br.com.threadstech.stockfy.entity.Payment;
import br.com.threadstech.stockfy.web.dto.serializer.CpfMaskSerializer;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import tools.jackson.databind.annotation.JsonSerialize;

import java.time.LocalDate;
import java.util.Set;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class CustomerDetailDto {
  private Long id;
  private String fullName;

  @JsonSerialize(using = CpfMaskSerializer.class)
  private String cpf;

  private LocalDate birthday;
  private CustomerContactDetailDto contact;
  private CustomerAddressDetailDto address;
}
