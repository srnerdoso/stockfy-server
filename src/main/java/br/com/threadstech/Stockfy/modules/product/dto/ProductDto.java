package br.com.threadstech.stockfy.modules.product.dto;

import br.com.threadstech.stockfy.modules.product.ProductType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public class ProductDto {

  @NotBlank(message = "Bar code is required")
  @Size(min = 7, max = 20, message = "Invalid bar code")
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
