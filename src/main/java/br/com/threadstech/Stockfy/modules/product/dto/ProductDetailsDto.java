package br.com.threadstech.stockfy.modules.product.dto;

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
public class ProductDetailsDto {

  private String name;
  private String barcode;
  private BigDecimal stockQuantity;
  private BigDecimal minimumStock;
  private BigDecimal price;
  private BigDecimal cost;
  private BigDecimal discount;
  private String unitType;
}
