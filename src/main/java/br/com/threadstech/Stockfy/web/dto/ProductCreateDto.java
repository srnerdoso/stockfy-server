package br.com.threadstech.stockfy.web.dto;

import br.com.threadstech.stockfy.validation.ProductType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
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
public class ProductCreateDto {

  @NotBlank(message = "{NotBlank.productDto.barCode}")
  @Size(min = 7, max = 20, message = "{Size.productDto.barCode}")
  private String barCode;

  @NotBlank(message = "{NotBlank.productDto.name}")
  @Size(min = 1, max = 255, message = "{Size.productDto.name}")
  private String name;

  @Positive(message = "{Positive.productDto.stock}")
  private BigDecimal stock;

  @PositiveOrZero(message = "{PositiveOrZero.productDto.minStock}")
  private BigDecimal minStock;

  @Positive(message = "{Positive.productDto.price}")
  private BigDecimal price;

  @Positive(message = "{Positive.productDto.cost}")
  private BigDecimal cost;

  @Positive(message = "{Positive.productDto.profit}")
  private BigDecimal profit;

  @PositiveOrZero(message = "{PositiveOrZero.productDto.discount}")
  private BigDecimal discount;

  @PositiveOrZero(message = "{PositiveOrZero.productDto.discountPercentage}")
  private BigDecimal discountPercentage;

  @NotNull(message = "{NotNull.productDto.type}")
  @ProductType(message = "{ProductType.productDto.type}")
  private String type;
}
