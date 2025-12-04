package br.com.threadstech.stockfy.web.dto;

import java.math.BigDecimal;
import java.util.Set;

import br.com.threadstech.stockfy.validation.PaymentMethod;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
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
public class PaymentCreateDto {

  @NotBlank(message = "{NotBlank.paymentCreateDto.paymentMethod}")
  @PaymentMethod(message = "{PaymentMethod.paymentCreateDto.paymentMethod}")
  private String paymentMethod;

  @Valid
  @NotNull(message = "{NotNull.paymentCreateDto.carts}")
  private Set<CartCreateDto> cart;

  @NotNull(message = "{NotNull.paymentCreateDto.total}")
  @PositiveOrZero(message = "{PositiveOrZero.paymentCreateDto.total}")
  private BigDecimal total;
}
