package br.com.threadstech.stockfy.web.dto;

import br.com.threadstech.stockfy.enums.ProductType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertDto {
  private String type;
  private String product;
  private ProductType productType;
  private Double stock;
}
