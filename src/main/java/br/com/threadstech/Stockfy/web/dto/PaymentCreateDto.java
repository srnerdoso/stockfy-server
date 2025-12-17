package br.com.threadstech.stockfy.web.dto;

import br.com.threadstech.stockfy.validation.PaymentMethod;
import br.com.threadstech.stockfy.validation.PaymentStatus;
import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.util.Set;
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
public class PaymentCreateDto {

  @Nullable
  @Positive(message = "{Positive.paymentCreateDto.customerId}")
  private Long customerId;

  @NotBlank(message = "{NotBlank.paymentCreateDto.paymentMethod}")
  @PaymentMethod(message = "{PaymentMethod.paymentCreateDto.paymentMethod}")
  private String paymentMethod;

  @PaymentStatus(message = "{PaymentStatus.paymentCreateDto.paymentStatus}")
  private String paymentStatus;

  @Valid
  @NotNull(message = "{NotNull.paymentCreateDto.carts}")
  private Set<CartCreateDto> cart;

  @NotNull(message = "{NotNull.paymentCreateDto.total}")
  @PositiveOrZero(message = "{PositiveOrZero.paymentCreateDto.total}")
  private BigDecimal total;
}
