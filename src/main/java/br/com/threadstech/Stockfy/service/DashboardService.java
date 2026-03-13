package br.com.threadstech.stockfy.service;

import br.com.threadstech.stockfy.entity.Cart;
import br.com.threadstech.stockfy.entity.Payment;
import br.com.threadstech.stockfy.enums.AuditI18nKeys;
import br.com.threadstech.stockfy.enums.MetricI18nKeys;
import br.com.threadstech.stockfy.enums.PaymentStatus;
import br.com.threadstech.stockfy.enums.ProductType;
import br.com.threadstech.stockfy.web.dto.AlertDto;
import br.com.threadstech.stockfy.web.dto.AuditDto;
import br.com.threadstech.stockfy.web.dto.LastSaleDto;
import br.com.threadstech.stockfy.web.dto.MetricResponseDto;
import br.com.threadstech.stockfy.web.dto.SalesGraphDto;
import br.com.threadstech.stockfy.web.dto.TopProductDto;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DashboardService {

  private final PaymentService paymentService;

  @Transactional(readOnly = true)
  public List<MetricResponseDto> getMetrics() {
    List<Payment> paidPayments = paymentService.findAllByStatus(PaymentStatus.PAID);

    return List.of(
        MetricResponseDto.builder()
            .type("monetary")
            .i18nKey(MetricI18nKeys.TOTAL_SALES)
            .value(calculateTotalSales(paidPayments))
            .percentage(0.0)
            .build(),
        MetricResponseDto.builder()
            .type("base")
            .i18nKey(MetricI18nKeys.TOTAL_PROFIT)
            .value(calculateTotalProfit(paidPayments))
            .percentage(0.0)
            .build(),
        MetricResponseDto.builder()
            .type("non-percentage")
            .i18nKey(MetricI18nKeys.TOTAL_CUSTOMERS)
            .value(calculateTotalCustomers(paidPayments))
            .build());
  }

  private Double calculateTotalSales(List<Payment> payments) {
    return payments.stream()
        .map(Payment::getTotal)
        .reduce(BigDecimal.ZERO, BigDecimal::add)
        .doubleValue();
  }

  private Double calculateTotalProfit(List<Payment> payments) {
    return payments.stream()
        .flatMap(payment -> payment.getCart().stream())
        .map(this::calculateCartProfit)
        .reduce(BigDecimal.ZERO, BigDecimal::add)
        .doubleValue();
  }

  private BigDecimal calculateCartProfit(Cart cart) {
    BigDecimal cost = cart.getProduct().getCost();
    BigDecimal quantity = cart.getQuantity();
    BigDecimal paymentValue = cart.getPaymentValue();
    return paymentValue.subtract(cost.multiply(quantity));
  }

  private Double calculateTotalCustomers(List<Payment> payments) {
    return (double)
        payments.stream().map(Payment::getCustomer).filter(Objects::nonNull).distinct().count();
  }

  public List<TopProductDto> getTopProducts() {
    return List.of(
        new TopProductDto("Product A", 150L),
        new TopProductDto("Product B", 120L),
        new TopProductDto("Product C", 90L));
  }

  public List<LastSaleDto> getLastSales() {
    return List.of(
        LastSaleDto.builder()
            .id(1L)
            .customerId(101L)
            .value(new BigDecimal("250.00"))
            .employeeName("John Doe")
            .date("2024-03-20")
            .time("14:30")
            .build());
  }

  public List<AuditDto> getAuditEvents() {
    return List.of(
        AuditDto.builder()
            .actionId(1L)
            .i18nKey(AuditI18nKeys.PRODUCT_CREATED)
            .timestamp(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME))
            .employeeName("Admin")
            .build());
  }

  public List<AlertDto> getAlerts() {
    return List.of(
        AlertDto.builder()
            .type("low-stock")
            .product("Product X")
            .productType(ProductType.UNIT)
            .stock(5.0)
            .build());
  }

  public List<SalesGraphDto> getSalesGraph() {
    return List.of(
        new SalesGraphDto(10L, "2024-03-14"),
        new SalesGraphDto(15L, "2024-03-15"),
        new SalesGraphDto(8L, "2024-03-16"));
  }
}
