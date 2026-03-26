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
public class ProductSummaryDto {
  // FIXME: Estes campos devem ser ajustados
  private Long id;
  private String name;
  private BigDecimal price;
}
