package br.com.threadstech.stockfy.web.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.domain.Pageable;

/**
 * DTO minimizado de cliente. Geralmente utilizado em listagem de clientes.
 *
 * @see br.com.threadstech.stockfy.web.controller.CustomerController#findAll(Pageable)
 *     CustomerController#findAll
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class CustomerSummaryDto {
  private Long id;
  private String fullName;
  private String email;
  private String phoneNumber;
}
