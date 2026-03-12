package br.com.threadstech.stockfy.web.controller;

import br.com.threadstech.stockfy.api.ApiPaths;
import br.com.threadstech.stockfy.service.DashboardService;
import br.com.threadstech.stockfy.web.dto.AlertDto;
import br.com.threadstech.stockfy.web.dto.AuditDto;
import br.com.threadstech.stockfy.web.dto.LastSaleDto;
import br.com.threadstech.stockfy.web.dto.MetricResponseDto;
import br.com.threadstech.stockfy.web.dto.SalesGraphDto;
import br.com.threadstech.stockfy.web.dto.TopProductDto;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ApiPaths.DASHBOARD)
@RequiredArgsConstructor
public class DashboardController {

  private final DashboardService dashboardService;

  @GetMapping("/metrics")
  public ResponseEntity<List<MetricResponseDto>> getMetrics() {
    return ResponseEntity.ok(dashboardService.getMetrics());
  }

  @GetMapping("/top-products")
  public ResponseEntity<List<TopProductDto>> getTopProducts() {
    return ResponseEntity.ok(dashboardService.getTopProducts());
  }

  @GetMapping("/last-sales")
  public ResponseEntity<List<LastSaleDto>> getLastSales() {
    return ResponseEntity.ok(dashboardService.getLastSales());
  }

  @GetMapping("/audit-events")
  public ResponseEntity<List<AuditDto>> getAuditEvents() {
    return ResponseEntity.ok(dashboardService.getAuditEvents());
  }

  @GetMapping("/alerts")
  public ResponseEntity<List<AlertDto>> getAlerts() {
    return ResponseEntity.ok(dashboardService.getAlerts());
  }

  @GetMapping("/sales-graph")
  public ResponseEntity<List<SalesGraphDto>> getSalesGraph() {
    return ResponseEntity.ok(dashboardService.getSalesGraph());
  }
}
