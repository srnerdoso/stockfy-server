package br.com.threadstech.stockfy.service.scheduler;

import br.com.threadstech.stockfy.service.MetricDailyService;
import br.com.threadstech.stockfy.service.MetricMonthlyService;
import br.com.threadstech.stockfy.service.MetricYearlyService;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MetricScheduler {

  private final MetricDailyService metricDailyService;
  private final MetricMonthlyService metricMonthlyService;
  private final MetricYearlyService metricYearlyService;

  @Scheduled(cron = "${scheduler.metric.cron}")
  public void processMetrics() {
    processMetricsForDate(LocalDate.now());
  }

  public void processMetricsForDate(LocalDate today) {
    log.info("Processing metrics for {}", today);

    metricDailyService.calculateAndSave(today.minusDays(1));

    if (today.getDayOfMonth() == 1) {
      metricMonthlyService.calculateAndSave(today.minusMonths(1));
    }

    if (today.getDayOfYear() == 1) {
      metricYearlyService.calculateAndSave(today.minusYears(1));
    }
  }
}
