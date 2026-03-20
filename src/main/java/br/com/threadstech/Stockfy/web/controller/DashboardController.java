package br.com.threadstech.stockfy.web.controller;

import br.com.threadstech.stockfy.api.ApiPaths;
import br.com.threadstech.stockfy.service.DashboardService;
import br.com.threadstech.stockfy.web.doc.DashboardControllerDoc;
import br.com.threadstech.stockfy.web.dto.AlertDto;
import br.com.threadstech.stockfy.web.dto.AuditDto;
import br.com.threadstech.stockfy.web.dto.LastSaleDto;
import br.com.threadstech.stockfy.web.dto.MetricResponseDto;
import br.com.threadstech.stockfy.web.dto.SalesGraphDto;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ApiPaths.DASHBOARD_V1)
@RequiredArgsConstructor
public class DashboardController implements DashboardControllerDoc {

  private final DashboardService dashboardService;

  @Override
  @GetMapping("/metrics")
  public ResponseEntity<List<MetricResponseDto>> getMetrics() {
    return ResponseEntity.ok(dashboardService.getMetrics());
  }

  @Override
  @GetMapping("/sales")
  public ResponseEntity<List<SalesGraphDto>> getSalesGraph(
      @RequestParam(name = "filter", defaultValue = "days") String filter) {
    return ResponseEntity.ok(dashboardService.getSalesGraph(filter));
  }

  @Override
  @GetMapping("/last-sales")
  public ResponseEntity<List<LastSaleDto>> getLastSales() {
    return ResponseEntity.ok(dashboardService.getLastSales());
  }

  @Override
  @GetMapping("/audit")
  public ResponseEntity<List<AuditDto>> getAuditEvents() {
    return ResponseEntity.ok(dashboardService.getAuditEvents());
  }

  @Override
  @GetMapping("/alerts")
  public ResponseEntity<List<AlertDto>> getAlerts() {
    return ResponseEntity.ok(dashboardService.getAlerts());
  }
}
