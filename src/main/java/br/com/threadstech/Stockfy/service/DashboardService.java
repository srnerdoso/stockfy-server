package br.com.threadstech.stockfy.service;

import br.com.threadstech.stockfy.enums.AuditI18nKeys;
import br.com.threadstech.stockfy.enums.MetricI18nKeys;
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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

// FIXME: Implementar buscas reais ao banco. Atualmente os dados retornados são dados mock sem valor
//        real
@Service
@RequiredArgsConstructor
public class DashboardService {

  public List<MetricResponseDto> getMetrics() {
    return List.of(
        MetricResponseDto.builder()
            .type("monetary")
            .i18nKey(MetricI18nKeys.TOTAL_SALES)
            .value(15000.50)
            .percentage(12.5)
            .build(),
        MetricResponseDto.builder()
            .type("base")
            .i18nKey(MetricI18nKeys.TOTAL_PROFIT)
            .value(4500.0)
            .percentage(5.2)
            .build(),
        MetricResponseDto.builder()
            .type("non-percentage")
            .i18nKey(MetricI18nKeys.TOTAL_CUSTOMERS)
            .value(85.0)
            .build());
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
