package br.com.threadstech.stockfy.web.dto;

import java.math.BigDecimal;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ProductResponseDto {

  private String name;
  private BigDecimal stock;
  private BigDecimal price;
  private BigDecimal cost;
  private BigDecimal profit;
  private BigDecimal discount;
  private BigDecimal discountPercentage;
  private String type;
}