package br.com.threadstech.stockfy.modules.product.dto;

import br.com.threadstech.stockfy.core.validation.Barcode;
import br.com.threadstech.stockfy.core.validation.UnitType;
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

  @NotBlank(message = "{NotBlank.productDto.name}")
  @Size(min = 1, max = 255, message = "{Size.productDto.name}")
  private String name;

  @NotBlank(message = "{NotBlank.productDto.barCode}")
  @Barcode(message = "{Barcode.productDto.barCode}")
  private String barcode;

  @NotNull(message = "{NotNull.productDto.stock}")
  @Positive(message = "{Positive.productDto.stock}")
  private BigDecimal stockQuantity;

  @NotNull(message = "{NotNull.productDto.minStock}")
  @PositiveOrZero(message = "{PositiveOrZero.productDto.minStock}")
  private BigDecimal minimumStock;

  @NotNull(message = "{NotNull.productDto.price}")
  @Positive(message = "{Positive.productDto.price}")
  private BigDecimal price;

  @NotNull(message = "{NotNull.productDto.cost}")
  @Positive(message = "{Positive.productDto.cost}")
  private BigDecimal cost;

  @PositiveOrZero(message = "{PositiveOrZero.productDto.discount}")
  private BigDecimal discount;

  @NotNull(message = "{NotNull.productDto.type}")
  @UnitType(message = "{UnitType.productDto.type}")
  private String unitType;
}
