package br.com.threadstech.stockfy.web.dto;

import br.com.threadstech.stockfy.enums.ProductType;
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
public class ProductDetailResponseDto {

  private String barCode;
  private String name;
  private BigDecimal stock;
  private BigDecimal price;
  private BigDecimal cost;
  private BigDecimal profit;
  private BigDecimal discount;
  private BigDecimal discountPercentage;
  private ProductType type;
}
