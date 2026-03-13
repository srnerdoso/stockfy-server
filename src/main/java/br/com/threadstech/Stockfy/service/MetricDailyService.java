package br.com.threadstech.stockfy.service;

import br.com.threadstech.stockfy.entity.MetricDaily;
import br.com.threadstech.stockfy.enums.PaymentStatus;
import br.com.threadstech.stockfy.repository.CustomerRepository;
import br.com.threadstech.stockfy.repository.MetricDailyRepository;
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
public class MetricDailyService {

  private final MetricDailyRepository metricDailyRepository;
  private final PaymentRepository paymentRepository;
  private final CustomerRepository customerRepository;

  @Transactional
  public void calculateAndSave(LocalDate date) {
    Instant start = date.atStartOfDay(ZoneId.systemDefault()).toInstant();
    Instant end = date.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant();

    long customersCount = customerRepository.countByCreatedAtBetween(start, end);
    long purchasesCount = paymentRepository.countByPaymentStatusAndCreatedAtBetween(PaymentStatus.PAID, start, end);
    BigDecimal profit = Optional.ofNullable(
        paymentRepository.calculateProfitByStatusAndCreatedAtBetween(PaymentStatus.PAID, start, end)
    ).orElse(BigDecimal.ZERO);

    MetricDaily metric = new MetricDaily();
    metric.setDate(date);
    metric.setNewCustomersCount(customersCount);
    metric.setPurchasesCount(purchasesCount);
    metric.setProfit(profit);
    metric.setExpenses(BigDecimal.ZERO);

    metricDailyRepository.save(metric);
  }
}
