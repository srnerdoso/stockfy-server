package br.com.threadstech.stockfy.modules.product.dto;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductSaleDto implements ProductResponseV1_1 {
  private String name;
  private BigDecimal price;
  private String barcode;
}
