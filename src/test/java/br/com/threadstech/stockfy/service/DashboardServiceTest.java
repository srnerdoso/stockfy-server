package br.com.threadstech.stockfy.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import br.com.threadstech.stockfy.entity.Cart;
import br.com.threadstech.stockfy.entity.Customer;
import br.com.threadstech.stockfy.entity.Payment;
import br.com.threadstech.stockfy.entity.Product;
import br.com.threadstech.stockfy.enums.MetricI18nKeys;
import br.com.threadstech.stockfy.enums.PaymentStatus;
import br.com.threadstech.stockfy.web.dto.MetricResponseDto;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class DashboardServiceTest {

  @Mock private PaymentService paymentService;

  @InjectMocks private DashboardService dashboardService;

  private Payment paidPayment;
  private Customer customer;

  @BeforeEach
  void setUp() {
    customer = new Customer();
    customer.setId(1L);

    Product product = new Product();
    product.setId(1L);
    product.setCost(new BigDecimal("40.00"));

    Cart cart = new Cart();
    cart.setProduct(product);
    cart.setQuantity(new BigDecimal("2.000"));
    cart.setPaymentValue(new BigDecimal("100.00")); // Sale price 50.00 * 2

    paidPayment = new Payment();
    paidPayment.setId(100L);
    paidPayment.setPaymentStatus(PaymentStatus.PAID);
    paidPayment.setCustomer(customer);
    paidPayment.setTotal(new BigDecimal("100.00"));
    paidPayment.setCart(Set.of(cart));
  }

  @Test
  @DisplayName("Should calculate metrics correctly when there are paid payments")
  void shouldCalculateMetricsCorrectly() {
    when(paymentService.findAllByStatus(PaymentStatus.PAID)).thenReturn(List.of(paidPayment));

    List<MetricResponseDto> metrics = dashboardService.getMetrics();

    assertThat(metrics).hasSize(3);

    // TOTAL_SALES
    MetricResponseDto salesMetric = metrics.stream()
        .filter(m -> m.getI18nKey() == MetricI18nKeys.TOTAL_SALES)
        .findFirst().get();
    assertThat(salesMetric.getValue()).isEqualTo(100.0);

    // TOTAL_PROFIT
    // Profit = paymentValue - (cost * quantity) = 100.00 - (40.00 * 2) = 20.00
    MetricResponseDto profitMetric = metrics.stream()
        .filter(m -> m.getI18nKey() == MetricI18nKeys.TOTAL_PROFIT)
        .findFirst().get();
    assertThat(profitMetric.getValue()).isEqualTo(20.0);

    // TOTAL_CUSTOMERS
    MetricResponseDto customersMetric = metrics.stream()
        .filter(m -> m.getI18nKey() == MetricI18nKeys.TOTAL_CUSTOMERS)
        .findFirst().get();
    assertThat(customersMetric.getValue()).isEqualTo(1.0);
  }

  @Test
  @DisplayName("Should return zero metrics when there are no paid payments")
  void shouldReturnZeroMetricsWhenNoPayments() {
    when(paymentService.findAllByStatus(PaymentStatus.PAID)).thenReturn(Collections.emptyList());

    List<MetricResponseDto> metrics = dashboardService.getMetrics();

    assertThat(metrics).hasSize(3);
    metrics.forEach(m -> {
      if (m.getI18nKey() != MetricI18nKeys.TOTAL_CUSTOMERS) {
         assertThat(m.getValue()).isEqualTo(0.0);
      } else {
         assertThat(m.getValue()).isEqualTo(0.0);
      }
    });
  }
}
