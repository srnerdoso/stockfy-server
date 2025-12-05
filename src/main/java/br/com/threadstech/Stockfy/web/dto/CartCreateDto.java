package br.com.threadstech.stockfy.web.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
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
public class CartCreateDto {

  // TODO: No mapper este campo deve ser skipped
  @NotNull(message = "{NotNull.cartDto.productId}")
  @Positive(message = "{Positive.cartDto.productId}")
  private Long productId;

  @NotNull(message = "{NotNull.cartDto.quantity}")
  @Positive(message = "{Positive.cartDto.quantity}")
  private BigDecimal quantity;

  @NotNull(message = "{NotNull.cartDto.paymentValue}")
  @PositiveOrZero(message = "{PositiveOrZero.cartDto.paymentValue}")
  private BigDecimal paymentValue;
}
