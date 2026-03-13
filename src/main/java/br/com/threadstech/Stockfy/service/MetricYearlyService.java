package br.com.threadstech.stockfy.service;

import br.com.threadstech.stockfy.entity.MetricYearly;
import br.com.threadstech.stockfy.enums.PaymentStatus;
import br.com.threadstech.stockfy.repository.CustomerRepository;
import br.com.threadstech.stockfy.repository.MetricYearlyRepository;
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
public class MetricYearlyService {

  private final MetricYearlyRepository metricYearlyRepository;
  private final PaymentRepository paymentRepository;
  private final CustomerRepository customerRepository;

  @Transactional
  public void calculateAndSave(LocalDate date) {
    LocalDate firstDay = date.withDayOfYear(1);
    Instant start = firstDay.atStartOfDay(ZoneId.systemDefault()).toInstant();
    Instant end = firstDay.plusYears(1).atStartOfDay(ZoneId.systemDefault()).toInstant();

    long customersCount = customerRepository.countByCreatedAtBetween(start, end);
    long purchasesCount = paymentRepository.countByPaymentStatusAndCreatedAtBetween(PaymentStatus.PAID, start, end);
    BigDecimal profit = Optional.ofNullable(
        paymentRepository.calculateProfitByStatusAndCreatedAtBetween(PaymentStatus.PAID, start, end)
    ).orElse(BigDecimal.ZERO);

    MetricYearly metric = new MetricYearly();
    metric.setDate(firstDay);
    metric.setNewCustomersCount(customersCount);
    metric.setPurchasesCount(purchasesCount);
    metric.setProfit(profit);
    metric.setExpenses(BigDecimal.ZERO);

    metricYearlyRepository.save(metric);
  }
}
