package br.com.threadstech.stockfy.service;

import br.com.threadstech.stockfy.entity.MetricMonthly;
import br.com.threadstech.stockfy.enums.PaymentStatus;
import br.com.threadstech.stockfy.repository.CustomerRepository;
import br.com.threadstech.stockfy.repository.MetricMonthlyRepository;
import br.com.threadstech.stockfy.repository.PaymentRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MetricMonthlyService {

  private final MetricMonthlyRepository metricMonthlyRepository;
  private final PaymentRepository paymentRepository;
  private final CustomerRepository customerRepository;

  @Transactional
  public void calculateAndSave(LocalDate date) {
    LocalDate firstDay = date.withDayOfMonth(1);
    Instant start = firstDay.atStartOfDay(ZoneId.systemDefault()).toInstant();
    Instant end = firstDay.plusMonths(1).atStartOfDay(ZoneId.systemDefault()).toInstant();

    long customersCount = customerRepository.countByCreatedAtBetween(start, end);
    long purchasesCount = paymentRepository.countByPaymentStatusAndCreatedAtBetween(PaymentStatus.PAID, start, end);
    BigDecimal profit = Optional.ofNullable(
        paymentRepository.calculateProfitByStatusAndCreatedAtBetween(PaymentStatus.PAID, start, end)
    ).orElse(BigDecimal.ZERO);
    BigDecimal totalSales = Optional.ofNullable(
        paymentRepository.calculateTotalSalesByStatusAndCreatedAtBetween(PaymentStatus.PAID, start, end)
    ).orElse(BigDecimal.ZERO);

    MetricMonthly metric = new MetricMonthly();
    metric.setDate(firstDay);
    metric.setNewCustomersCount(customersCount);
    metric.setPurchasesCount(purchasesCount);
    metric.setProfit(profit);
    metric.setTotalSales(totalSales);
    metric.setExpenses(BigDecimal.ZERO);

    metricMonthlyRepository.save(metric);
  }
}
