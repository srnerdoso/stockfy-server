package br.com.threadstech.stockfy.web.dto;

import java.math.BigDecimal;

import br.com.threadstech.stockfy.validation.ProductType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
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
public class ProductDto {

  @NotBlank
  @Size(min = 7, max = 20)
  private String barCode;

  @NotBlank
  @Size(min = 1, max = 255)
  private String name;

  @Positive
  private BigDecimal stock;

  @Positive
  private BigDecimal price;

  @Positive
  private BigDecimal cost;

  @Positive
  private BigDecimal profit;

  @PositiveOrZero
  private BigDecimal discount;

  @PositiveOrZero
  private BigDecimal discountPercentage;

  @NotNull
  @ProductType
  private String type;
}
