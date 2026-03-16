package br.com.threadstech.stockfy.entity;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@MappedSuperclass
@NoArgsConstructor
@AllArgsConstructor
public abstract class Metric {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "purchases_count", nullable = false, updatable = false)
  private Long purchasesCount;

  @Column(name = "new_customers_count", nullable = false, updatable = false)
  private Long newCustomersCount;

  @Column(name = "total_sales", nullable = false, updatable = false, precision = 10, scale = 2)
  private BigDecimal totalSales;

  @Column(name = "profit", nullable = false, updatable = false, precision = 10, scale = 2)
  private BigDecimal profit;

  // FIXME: Implementar lógica real de despesas. Por enquanto o valor é 0.
  @Column(name = "expenses", nullable = false, updatable = false, precision = 10, scale = 2)
  private BigDecimal expenses = BigDecimal.ZERO;

  @Column(name = "date", nullable = false, updatable = false)
  private LocalDate date;
}
