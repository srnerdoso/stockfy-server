package br.com.threadstech.stockfy.service;

import br.com.threadstech.stockfy.entity.AuditRevisionEntity;
import br.com.threadstech.stockfy.entity.Customer;
import br.com.threadstech.stockfy.entity.Employee;
import br.com.threadstech.stockfy.entity.Metric;
import br.com.threadstech.stockfy.entity.MetricMonthly;
import br.com.threadstech.stockfy.entity.Payment;
import br.com.threadstech.stockfy.entity.Product;
import br.com.threadstech.stockfy.enums.AuditI18nKeys;
import br.com.threadstech.stockfy.enums.MetricI18nKeys;
import br.com.threadstech.stockfy.enums.PaymentStatus;
import br.com.threadstech.stockfy.enums.ProductType;
import br.com.threadstech.stockfy.repository.CustomerRepository;
import br.com.threadstech.stockfy.repository.EmployeeRepository;
import br.com.threadstech.stockfy.repository.MetricDailyRepository;
import br.com.threadstech.stockfy.repository.MetricMonthlyRepository;
import br.com.threadstech.stockfy.repository.MetricYearlyRepository;
import br.com.threadstech.stockfy.repository.PaymentRepository;
import br.com.threadstech.stockfy.repository.ProductRepository;
import br.com.threadstech.stockfy.web.dto.AlertDto;
import br.com.threadstech.stockfy.web.dto.mapper.AlertMapper;
import br.com.threadstech.stockfy.web.dto.AuditDto;
import br.com.threadstech.stockfy.web.dto.LastSaleDto;
import br.com.threadstech.stockfy.web.dto.MetricResponseDto;
import br.com.threadstech.stockfy.web.dto.SalesGraphDto;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.RevisionType;
import org.hibernate.envers.query.AuditEntity;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DashboardService {

  private final MetricDailyRepository metricDailyRepository;
  private final MetricMonthlyRepository metricMonthlyRepository;
  private final MetricYearlyRepository metricYearlyRepository;
  private final PaymentRepository paymentRepository;
  private final ProductRepository productRepository;
  private final CustomerRepository customerRepository;
  private final EmployeeRepository employeeRepository;
  private final AlertService alertService;
  private final AlertMapper alertMapper;
  private final EntityManager entityManager;

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
    AuditReader reader = AuditReaderFactory.get(entityManager);

    List<AuditDto> productAudits = getRevisions(reader, Product.class);
    List<AuditDto> paymentAudits = getRevisions(reader, Payment.class);
    List<AuditDto> employeeAudits = getRevisions(reader, Employee.class);
    List<AuditDto> customerAudits = getRevisions(reader, Customer.class);

    return Stream.of(productAudits, paymentAudits, employeeAudits, customerAudits)
        .flatMap(List::stream)
        .sorted(Comparator.comparing(AuditDto::getTimestamp).reversed())
        .limit(20)
        .toList();
  }

  @SuppressWarnings("unchecked")
  private <T> List<AuditDto> getRevisions(AuditReader reader, Class<T> clazz) {
    List<Object[]> revisions = reader.createQuery()
        .forRevisionsOfEntity(clazz, false, true)
        .addOrder(AuditEntity.revisionNumber().desc())
        .setMaxResults(20)
        .getResultList();

    return revisions.stream()
        .map(rev -> {
          AuditRevisionEntity revEntity = (AuditRevisionEntity) rev[1];
          RevisionType type = (RevisionType) rev[2];

          return AuditDto.builder()
              .audId((long) revEntity.getId())
              .i18nKey(mapToI18nKey(clazz, type))
              .timestamp(LocalDateTime.ofInstant(
                      Instant.ofEpochMilli(revEntity.getTimestamp()),
                      ZoneId.systemDefault())
                  .format(DateTimeFormatter.ISO_DATE_TIME))
              .employeeName(revEntity.getUsername())
              .build();
        })
        .toList();
  }

  private AuditI18nKeys mapToI18nKey(Class<?> clazz, RevisionType type) {
    if (clazz == Product.class) {
      return switch (type) {
        case ADD -> AuditI18nKeys.PRODUCT_CREATED;
        case MOD -> AuditI18nKeys.PRODUCT_UPDATED;
        case DEL -> AuditI18nKeys.PRODUCT_DELETED;
      };
    } else if (clazz == Payment.class) {
      return switch (type) {
        case ADD -> AuditI18nKeys.SALE_COMPLETED;
        case MOD -> AuditI18nKeys.SALE_UPDATED;
        case DEL -> AuditI18nKeys.SALE_DELETED;
      };
    } else if (clazz == Employee.class) {
      return switch (type) {
        case ADD -> AuditI18nKeys.EMPLOYEE_CREATED;
        case MOD -> AuditI18nKeys.EMPLOYEE_UPDATED;
        case DEL -> AuditI18nKeys.EMPLOYEE_DELETED;
      };
    } else if (clazz == Customer.class) {
      return switch (type) {
        case ADD -> AuditI18nKeys.CUSTOMER_CREATED;
        case MOD -> AuditI18nKeys.CUSTOMER_UPDATED;
        case DEL -> AuditI18nKeys.CUSTOMER_DELETED;
      };
    }
    throw new IllegalArgumentException("Unknown entity class: " + clazz.getName());
  }

  @Transactional(readOnly = true)
  public List<AlertDto> getAlerts() {
    return alertMapper.toDtoList(alertService.findAll());
  }
}
