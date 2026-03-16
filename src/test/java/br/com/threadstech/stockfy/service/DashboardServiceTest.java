package br.com.threadstech.stockfy.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import br.com.threadstech.stockfy.entity.AuditLog;
import br.com.threadstech.stockfy.entity.MetricMonthly;
import br.com.threadstech.stockfy.entity.Payment;
import br.com.threadstech.stockfy.entity.Product;
import br.com.threadstech.stockfy.entity.TopProductMetric;
import br.com.threadstech.stockfy.enums.AuditI18nKeys;
import br.com.threadstech.stockfy.enums.MetricI18nKeys;
import br.com.threadstech.stockfy.enums.ProductType;
import br.com.threadstech.stockfy.repository.AuditLogRepository;
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
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class DashboardServiceTest {

  @Mock private MetricDailyRepository metricDailyRepository;
  @Mock private MetricMonthlyRepository metricMonthlyRepository;
  @Mock private MetricYearlyRepository metricYearlyRepository;
  @Mock private TopProductMetricRepository topProductMetricRepository;
  @Mock private PaymentRepository paymentRepository;
  @Mock private AuditLogRepository auditLogRepository;
  @Mock private ProductRepository productRepository;

  @InjectMocks private DashboardService dashboardService;

  @Test
  @DisplayName("Should return metrics from monthly repository")
  void shouldReturnMetrics() {
    LocalDate now = LocalDate.now().withDayOfMonth(1);
    MetricMonthly current = new MetricMonthly();
    current.setTotalSales(new BigDecimal("1000.00"));
    current.setProfit(new BigDecimal("200.00"));
    current.setNewCustomersCount(10L);

    when(metricMonthlyRepository.findByDate(now)).thenReturn(Optional.of(current));
    when(metricMonthlyRepository.findByDate(now.minusMonths(1))).thenReturn(Optional.empty());

    List<MetricResponseDto> metrics = dashboardService.getMetrics();

    assertThat(metrics).hasSize(3);
    assertThat(metrics.get(0).getI18nKey()).isEqualTo(MetricI18nKeys.TOTAL_SALES);
    assertThat(metrics.get(0).getValue()).isEqualTo(1000.0);
    assertThat(metrics.get(0).getPercentage()).isEqualTo(100.0);
  }

  @Test
  @DisplayName("Should return top products from metric repository")
  void shouldReturnTopProducts() {
    LocalDate today = LocalDate.now();
    TopProductMetric topProduct = TopProductMetric.builder()
        .productName("Test Product")
        .salesCount(50L)
        .productType(ProductType.UNIT)
        .date(today)
        .build();

    when(topProductMetricRepository.findMaxDate()).thenReturn(Optional.of(today));
    when(topProductMetricRepository.findAllByProductTypeAndDateOrderBySalesCountDesc(ProductType.UNIT, today))
        .thenReturn(List.of(topProduct));

    List<br.com.threadstech.stockfy.web.dto.TopProductDto> result = dashboardService.getTopProducts(ProductType.UNIT);

    assertThat(result).hasSize(1);
    assertThat(result.get(0).getProductName()).isEqualTo("Test Product");
    assertThat(result.get(0).getSalesCount()).isEqualTo(50L);
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
    assertThat(result.get(0).getEmployeeName()).isEqualTo("John Doe");
  }

  @Test
  @DisplayName("Should return audit events from log repository")
  void shouldReturnAuditEvents() {
    AuditLog log = AuditLog.builder()
        .id(1L)
        .action(AuditI18nKeys.PRODUCT_CREATED)
        .timestamp(LocalDateTime.now())
        .employeeName("Admin")
        .build();

    when(auditLogRepository.findLatest(any())).thenReturn(List.of(log));

    List<AuditDto> result = dashboardService.getAuditEvents();

    assertThat(result).hasSize(1);
    assertThat(result.get(0).getI18nKey()).isEqualTo(AuditI18nKeys.PRODUCT_CREATED);
    assertThat(result.get(0).getEmployeeName()).isEqualTo("Admin");
  }

  @Test
  @DisplayName("Should return low stock alerts")
  void shouldReturnAlerts() {
    Product lowStockProduct = new Product();
    lowStockProduct.setName("Low Stock");
    lowStockProduct.setStock(new BigDecimal("5.000"));
    lowStockProduct.setType(ProductType.UNIT);

    Product normalStockProduct = new Product();
    normalStockProduct.setName("Normal Stock");
    normalStockProduct.setStock(new BigDecimal("15.000"));
    normalStockProduct.setType(ProductType.UNIT);

    when(productRepository.findAll()).thenReturn(List.of(lowStockProduct, normalStockProduct));

    List<AlertDto> result = dashboardService.getAlerts();

    assertThat(result).hasSize(1);
    assertThat(result.get(0).getProduct()).isEqualTo("Low Stock");
    assertThat(result.get(0).getStock()).isEqualTo(5.0);
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
