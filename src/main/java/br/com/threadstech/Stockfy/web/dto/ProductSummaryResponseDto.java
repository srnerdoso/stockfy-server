package br.com.threadstech.stockfy.web.dto;

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
public class ProductSummaryResponseDto {

  private String barCode;
  private String name;
  private BigDecimal stock;
  private BigDecimal minStock;
  private BigDecimal price;
  private String type;
}