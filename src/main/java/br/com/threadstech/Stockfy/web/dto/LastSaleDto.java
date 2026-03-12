package br.com.threadstech.stockfy.web.dto;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LastSaleDto {
  private Long id;
  private Long customerId;
  private BigDecimal value;
  private String employeeName;
  private String date;
  private String time;
}
