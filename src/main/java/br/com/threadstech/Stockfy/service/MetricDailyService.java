package br.com.threadstech.stockfy.service;

import br.com.threadstech.stockfy.entity.MetricDaily;
import br.com.threadstech.stockfy.entity.TopProductMetric;
import br.com.threadstech.stockfy.enums.PaymentStatus;
import br.com.threadstech.stockfy.enums.ProductType;
import br.com.threadstech.stockfy.repository.CustomerRepository;
import br.com.threadstech.stockfy.repository.MetricDailyRepository;
import br.com.threadstech.stockfy.repository.PaymentRepository;
import br.com.threadstech.stockfy.repository.TopProductMetricRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MetricDailyService {

  private final MetricDailyRepository metricDailyRepository;
  private final PaymentRepository paymentRepository;
  private final CustomerRepository customerRepository;
  private final TopProductMetricRepository topProductMetricRepository;

  @Transactional
  public void calculateAndSave(LocalDate date) {
    Instant start = date.atStartOfDay(ZoneId.systemDefault()).toInstant();
    Instant end = date.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant();

    long customersCount = customerRepository.countByCreatedAtBetween(start, end);
    long purchasesCount =
        paymentRepository.countByPaymentStatusAndCreatedAtBetween(PaymentStatus.PAID, start, end);
    BigDecimal profit =
        Optional.ofNullable(
                paymentRepository.calculateProfitByStatusAndCreatedAtBetween(
                    PaymentStatus.PAID, start, end))
            .orElse(BigDecimal.ZERO);
    BigDecimal totalSales =
        Optional.ofNullable(
                paymentRepository.calculateTotalSalesByStatusAndCreatedAtBetween(
                    PaymentStatus.PAID, start, end))
            .orElse(BigDecimal.ZERO);

    MetricDaily metric = new MetricDaily();
    metric.setDate(date);
    metric.setNewCustomersCount(customersCount);
    metric.setPurchasesCount(purchasesCount);
    metric.setProfit(profit);
    metric.setTotalSales(totalSales);
    metric.setExpenses(BigDecimal.ZERO);

    metricDailyRepository.save(metric);

    // Calculate and save top products for this day
    Arrays.stream(ProductType.values())
        .forEach(
            type -> {
              List<Object[]> topProducts =
                  paymentRepository.findTopProductsByStatusAndCreatedAtBetween(
                      PaymentStatus.PAID, start, end, type);

              topProducts.stream()
                  .limit(5)
                  .forEach(
                      obj -> {
                        TopProductMetric topProductMetric =
                            TopProductMetric.builder()
                                .productName((String) obj[0])
                                .salesCount(((BigDecimal) obj[1]).longValue())
                                .productType(type)
                                .date(date)
                                .build();
                        topProductMetricRepository.save(topProductMetric);
                      });
            });
  }
}
