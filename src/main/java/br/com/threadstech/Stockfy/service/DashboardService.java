package br.com.threadstech.stockfy.service;

import br.com.threadstech.stockfy.entity.AuditLog;
import br.com.threadstech.stockfy.entity.Metric;
import br.com.threadstech.stockfy.entity.MetricMonthly;
import br.com.threadstech.stockfy.entity.Payment;
import br.com.threadstech.stockfy.entity.TopProductMetric;
import br.com.threadstech.stockfy.enums.MetricI18nKeys;
import br.com.threadstech.stockfy.enums.PaymentStatus;
import br.com.threadstech.stockfy.enums.ProductType;
import br.com.threadstech.stockfy.repository.AuditLogRepository;
import br.com.threadstech.stockfy.repository.CustomerRepository;
import br.com.threadstech.stockfy.repository.EmployeeRepository;
import br.com.threadstech.stockfy.repository.MetricDailyRepository;
import br.com.threadstech.stockfy.repository.MetricMonthlyRepository;
import br.com.threadstech.stockfy.repository.MetricYearlyRepository;
import br.com.threadstech.stockfy.repository.PaymentRepository;
import br.com.threadstech.stockfy.repository.ProductRepository;
import br.com.threadstech.stockfy.repository.TopProductMetricRepository;
import br.com.threadstech.stockfy.web.dto.AlertDto;
import br.com.threadstech.stockfy.web.dto.AuditDto;
import br.com.threadstech.stockfy.web.dto.LastSaleDto;
import br.com.threadstech.stockfy.web.dto.MetricResponseDto;
import br.com.threadstech.stockfy.web.dto.SalesGraphDto;
import br.com.threadstech.stockfy.web.dto.TopProductDto;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DashboardService {

  private final MetricDailyRepository metricDailyRepository;
  private final MetricMonthlyRepository metricMonthlyRepository;
  private final MetricYearlyRepository metricYearlyRepository;
  private final TopProductMetricRepository topProductMetricRepository;
  private final PaymentRepository paymentRepository;
  private final AuditLogRepository auditLogRepository;
  private final ProductRepository productRepository;
  private final CustomerRepository customerRepository;
  private final EmployeeRepository employeeRepository;

  @Transactional(readOnly = true)
  public List<MetricResponseDto> getMetrics() {
    List<MetricResponseDto> metrics = new ArrayList<>();
    ZoneId zoneId = ZoneId.systemDefault();
    LocalDate today = LocalDate.now();

    // 1. Daily Revenue
    Instant todayStart = today.atStartOfDay(zoneId).toInstant();
    Instant todayEnd = today.plusDays(1).atStartOfDay(zoneId).toInstant();
    BigDecimal dailyRevenue =
        Optional.ofNullable(
                paymentRepository.calculateTotalSalesByStatusAndCreatedAtBetween(
                    PaymentStatus.PAID, todayStart, todayEnd))
            .orElse(BigDecimal.ZERO);

    // Previous: Yesterday 00:00 to Today 00:00
    Instant yesterdayStart = today.minusDays(1).atStartOfDay(zoneId).toInstant();
    Instant yesterdayEnd = todayStart;
    BigDecimal yesterdayRevenue =
        Optional.ofNullable(
                paymentRepository.calculateTotalSalesByStatusAndCreatedAtBetween(
                    PaymentStatus.PAID, yesterdayStart, yesterdayEnd))
            .orElse(BigDecimal.ZERO);

    metrics.add(
        MetricResponseDto.builder()
            .type("monetary")
            .i18nKey(MetricI18nKeys.DAILY_REVENUE)
            .value(dailyRevenue.doubleValue())
            .percentage(calculatePercentage(dailyRevenue, yesterdayRevenue))
            .build());

    // 2. Monthly Revenue
    LocalDate firstDayOfMonth = today.withDayOfMonth(1);
    Instant monthStart = firstDayOfMonth.atStartOfDay(zoneId).toInstant();
    Instant monthEnd = firstDayOfMonth.plusMonths(1).atStartOfDay(zoneId).toInstant();
    BigDecimal monthlyRevenue =
        Optional.ofNullable(
                paymentRepository.calculateTotalSalesByStatusAndCreatedAtBetween(
                    PaymentStatus.PAID, monthStart, monthEnd))
            .orElse(BigDecimal.ZERO);

    LocalDate firstDayOfLastMonth = firstDayOfMonth.minusMonths(1);
    Instant lastMonthStart = firstDayOfLastMonth.atStartOfDay(zoneId).toInstant();
    Instant lastMonthEnd = monthStart;
    BigDecimal lastMonthRevenue =
        Optional.ofNullable(
                paymentRepository.calculateTotalSalesByStatusAndCreatedAtBetween(
                    PaymentStatus.PAID, lastMonthStart, lastMonthEnd))
            .orElse(BigDecimal.ZERO);

    metrics.add(
        MetricResponseDto.builder()
            .type("monetary")
            .i18nKey(MetricI18nKeys.MONTHLY_REVENUE)
            .value(monthlyRevenue.doubleValue())
            .percentage(calculatePercentage(monthlyRevenue, lastMonthRevenue))
            .build());

    // 3. Customers Count
    metrics.add(
        MetricResponseDto.builder()
            .type("non-percentage")
            .i18nKey(MetricI18nKeys.CUSTOMERS_COUNT)
            .value((double) customerRepository.count())
            .build());

    // 4. Products Count
    metrics.add(
        MetricResponseDto.builder()
            .type("non-percentage")
            .i18nKey(MetricI18nKeys.PRODUCTS_COUNT)
            .value((double) productRepository.count())
            .build());

    // 5. Employees Count
    metrics.add(
        MetricResponseDto.builder()
            .type("non-percentage")
            .i18nKey(MetricI18nKeys.EMPLOYEES_COUNT)
            .value((double) employeeRepository.count())
            .build());

    return metrics;
  }

  private Double calculatePercentage(BigDecimal current, BigDecimal previous) {
    if (previous == null || previous.compareTo(BigDecimal.ZERO) == 0) {
      return current.compareTo(BigDecimal.ZERO) > 0 ? 100.0 : 0.0;
    }
    return current
        .subtract(previous)
        .divide(previous, 4, RoundingMode.HALF_UP)
        .multiply(BigDecimal.valueOf(100))
        .doubleValue();
  }

  @Transactional(readOnly = true)
  public List<SalesGraphDto> getSalesGraph(String filter) {
    LocalDate end = LocalDate.now();
    List<SalesGraphDto> result = new ArrayList<>();

    switch (filter.toLowerCase()) {
      case "days" -> {
        LocalDate start = end.minusDays(7);
        metricDailyRepository
            .findAllByDateBetweenOrderByDateAsc(start, end)
            .forEach(
                m ->
                    result.add(
                        new SalesGraphDto(
                            m.getPurchasesCount(),
                            m.getDate().format(DateTimeFormatter.ISO_DATE))));
      }
      case "month" -> {
        LocalDate start = end.minusMonths(6).withDayOfMonth(1);
        metricMonthlyRepository
            .findAllByDateBetweenOrderByDateAsc(start, end.withDayOfMonth(1))
            .forEach(
                m ->
                    result.add(
                        new SalesGraphDto(
                            m.getPurchasesCount(),
                            m.getDate().format(DateTimeFormatter.ofPattern("MM/yyyy")))));
      }
      case "year" -> {
        LocalDate start = end.minusYears(5).withDayOfYear(1);
        metricYearlyRepository
            .findAllByDateBetweenOrderByDateAsc(start, end.withDayOfYear(1))
            .forEach(
                m ->
                    result.add(
                        new SalesGraphDto(
                            m.getPurchasesCount(),
                            m.getDate().format(DateTimeFormatter.ofPattern("yyyy")))));
      }
      default -> {}
    }
    return result;
  }

  @Transactional(readOnly = true)
  public List<TopProductDto> getTopProducts(ProductType type) {
    return topProductMetricRepository
        .findMaxDate()
        .map(
            date ->
                topProductMetricRepository
                    .findAllByProductTypeAndDateOrderBySalesCountDesc(type, date)
                    .stream()
                    .map(
                        m ->
                            TopProductDto.builder()
                                .productName(m.getProductName())
                                .salesCount(m.getSalesCount())
                                .build())
                    .toList())
        .orElse(Collections.emptyList());
  }

  @Transactional(readOnly = true)
  public List<LastSaleDto> getLastSales() {
    List<Payment> lastSales = paymentRepository.findLastSales(PageRequest.of(0, 10));
    return lastSales.stream()
        .map(
            p ->
                LastSaleDto.builder()
                    .id(p.getId())
                    .customerId(p.getCustomer() != null ? p.getCustomer().getId() : null)
                    .value(p.getTotal())
                    .employeeName(p.getCreatedBy())
                    .date(
                        p.getCreatedAt()
                            .atZone(java.time.ZoneId.systemDefault())
                            .toLocalDate()
                            .toString())
                    .time(
                        p.getCreatedAt()
                            .atZone(java.time.ZoneId.systemDefault())
                            .toLocalTime()
                            .format(DateTimeFormatter.ofPattern("HH:mm")))
                    .build())
        .toList();
  }

  @Transactional(readOnly = true)
  public List<AuditDto> getAuditEvents() {
    List<AuditLog> logs = auditLogRepository.findLatest(PageRequest.of(0, 20));
    return logs.stream()
        .map(
            l ->
                AuditDto.builder()
                    .actionId(l.getId())
                    .i18nKey(l.getAction())
                    .timestamp(l.getTimestamp().format(DateTimeFormatter.ISO_DATE_TIME))
                    .employeeName(l.getEmployeeName())
                    .build())
        .toList();
  }

  @Transactional(readOnly = true)
  public List<AlertDto> getAlerts() {
    // Low stock alerts: stock < 10
    return productRepository.findAll().stream()
        .filter(p -> p.getStock().compareTo(BigDecimal.valueOf(10)) < 0)
        .map(
            p ->
                AlertDto.builder()
                    .type("low-stock")
                    .product(p.getName())
                    .productType(p.getType())
                    .stock(p.getStock().doubleValue())
                    .build())
        .toList();
  }
}
