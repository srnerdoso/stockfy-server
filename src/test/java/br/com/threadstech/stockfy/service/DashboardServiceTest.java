package br.com.threadstech.stockfy.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import br.com.threadstech.stockfy.entity.AuditRevisionEntity;
import br.com.threadstech.stockfy.entity.Customer;
import br.com.threadstech.stockfy.entity.Employee;
import br.com.threadstech.stockfy.entity.Payment;
import br.com.threadstech.stockfy.entity.Product;
import br.com.threadstech.stockfy.enums.AuditI18nKeys;
import br.com.threadstech.stockfy.enums.MetricI18nKeys;
import br.com.threadstech.stockfy.repository.CustomerRepository;
import br.com.threadstech.stockfy.repository.EmployeeRepository;
import br.com.threadstech.stockfy.repository.MetricDailyRepository;
import br.com.threadstech.stockfy.repository.MetricMonthlyRepository;
import br.com.threadstech.stockfy.repository.MetricYearlyRepository;
import br.com.threadstech.stockfy.repository.PaymentRepository;
import br.com.threadstech.stockfy.repository.ProductRepository;
import br.com.threadstech.stockfy.web.dto.AlertDto;
import br.com.threadstech.stockfy.web.dto.AuditDto;
import br.com.threadstech.stockfy.web.dto.LastSaleDto;
import br.com.threadstech.stockfy.web.dto.MetricResponseDto;
import br.com.threadstech.stockfy.web.dto.mapper.AlertMapper;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.RevisionType;
import org.hibernate.envers.query.AuditQuery;
import org.hibernate.envers.query.AuditQueryCreator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class DashboardServiceTest {

  @Mock private MetricDailyRepository metricDailyRepository;
  @Mock private MetricMonthlyRepository metricMonthlyRepository;
  @Mock private MetricYearlyRepository metricYearlyRepository;
  @Mock private PaymentRepository paymentRepository;
  @Mock private ProductRepository productRepository;
  @Mock private CustomerRepository customerRepository;
  @Mock private EmployeeRepository employeeRepository;
  @Mock private AlertService alertService;
  @Mock private AlertMapper alertMapper;
  @Mock private EntityManager entityManager;

  @InjectMocks private DashboardService dashboardService;

  @Test
  @DisplayName("Should return metrics from repositories")
  void shouldReturnMetrics() {
    when(paymentRepository.calculateTotalSalesByStatusAndCreatedAtBetween(any(), any(), any()))
        .thenReturn(new BigDecimal("500.00"))
        .thenReturn(new BigDecimal("400.00"))
        .thenReturn(new BigDecimal("10000.00"))
        .thenReturn(new BigDecimal("8000.00"));
    
    when(customerRepository.count()).thenReturn(10L);
    when(productRepository.count()).thenReturn(20L);
    when(employeeRepository.count()).thenReturn(5L);

    List<MetricResponseDto> metrics = dashboardService.getMetrics();

    assertThat(metrics).hasSize(5);
    assertThat(metrics.get(0).getI18nKey()).isEqualTo(MetricI18nKeys.DAILY_REVENUE);
    assertThat(metrics.get(1).getI18nKey()).isEqualTo(MetricI18nKeys.MONTHLY_REVENUE);
    assertThat(metrics.get(2).getI18nKey()).isEqualTo(MetricI18nKeys.CUSTOMERS_COUNT);
    assertThat(metrics.get(3).getI18nKey()).isEqualTo(MetricI18nKeys.PRODUCTS_COUNT);
    assertThat(metrics.get(4).getI18nKey()).isEqualTo(MetricI18nKeys.EMPLOYEES_COUNT);
  }

  @Test
  @DisplayName("Should return last sales from payment repository")
  void shouldReturnLastSales() {
    Payment payment = new Payment();
    payment.setId(1L);
    payment.setTotal(new BigDecimal("150.00"));
    payment.setCreatedAt(Instant.now());
    payment.setCreatedBy("John Doe");

    when(paymentRepository.findLastSales(any())).thenReturn(List.of(payment));

    List<LastSaleDto> result = dashboardService.getLastSales();

    assertThat(result).hasSize(1);
    assertThat(result.get(0).getValue()).isEqualTo(new BigDecimal("150.00"));
  }

  @Test
  @DisplayName("Should return audit events from Envers")
  void shouldReturnAuditEvents() {
    AuditReader auditReader = mock(AuditReader.class);
    AuditQueryCreator queryCreator = mock(AuditQueryCreator.class);
    AuditQuery query = mock(AuditQuery.class);

    AuditRevisionEntity revEntity = new AuditRevisionEntity();
    revEntity.setId(1);
    revEntity.setTimestamp(System.currentTimeMillis());
    revEntity.setUsername("Admin");

    Object[] revData = new Object[]{new Product(), revEntity, RevisionType.ADD};

    try (MockedStatic<AuditReaderFactory> readerFactory = mockStatic(AuditReaderFactory.class)) {
      readerFactory.when(() -> AuditReaderFactory.get(entityManager)).thenReturn(auditReader);
      when(auditReader.createQuery()).thenReturn(queryCreator);
      when(queryCreator.forRevisionsOfEntity(any(), eq(false), eq(true))).thenReturn(query);
      when(query.addOrder(any())).thenReturn(query);
      when(query.setMaxResults(any(Integer.class))).thenReturn(query);
      when(query.getResultList()).thenReturn(Collections.singletonList(revData));

      List<AuditDto> result = dashboardService.getAuditEvents();

      assertThat(result).isNotEmpty();
      assertThat(result.get(0).getI18nKey()).isEqualTo(AuditI18nKeys.PRODUCT_CREATED);
      assertThat(result.get(0).getEmployeeName()).isEqualTo("Admin");
      assertThat(result.get(0).getAudId()).isEqualTo(1L);
    }
  }

  @Test
  @DisplayName("Should return alerts from alert service")
  void shouldReturnAlerts() {
    AlertDto alertDto = AlertDto.builder().product("Test Product").build();
    when(alertService.findAll()).thenReturn(Collections.emptyList());
    when(alertMapper.toDtoList(any())).thenReturn(List.of(alertDto));

    List<AlertDto> result = dashboardService.getAlerts();

    assertThat(result).hasSize(1);
    assertThat(result.get(0).getProduct()).isEqualTo("Test Product");
  }

  @Test
  @DisplayName("Should return sales graph for days")
  void shouldReturnSalesGraphDays() {
    LocalDate today = LocalDate.now();
    br.com.threadstech.stockfy.entity.MetricDaily metric = new br.com.threadstech.stockfy.entity.MetricDaily();
    metric.setDate(today);
    metric.setPurchasesCount(10L);

    when(metricDailyRepository.findAllByDateBetweenOrderByDateAsc(any(), any()))
        .thenReturn(List.of(metric));

    List<br.com.threadstech.stockfy.web.dto.SalesGraphDto> result = dashboardService.getSalesGraph("days");

    assertThat(result).hasSize(1);
    assertThat(result.get(0).getSalesCount()).isEqualTo(10L);
    assertThat(result.get(0).getDate()).isEqualTo(today.format(DateTimeFormatter.ISO_DATE));
  }
}
