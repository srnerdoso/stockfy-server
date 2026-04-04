package br.com.threadstech.stockfy.modules.product.utils;

import br.com.threadstech.stockfy.modules.product.enums.StockStatus;
import java.math.BigDecimal;
import org.springframework.stereotype.Component;

@Component
public class StockStatusResolver {

  public StockStatus resolve(BigDecimal stockQuantity, BigDecimal minimumStock) {
    if (stockQuantity == null || stockQuantity.compareTo(BigDecimal.ZERO) <= 0) {
      return StockStatus.OUT_OF_STOCK;
    }
    if (stockQuantity.compareTo(minimumStock) <= 0) {
      return StockStatus.LOW_STOCK;
    }
    return StockStatus.IN_STOCK;
  }
}
