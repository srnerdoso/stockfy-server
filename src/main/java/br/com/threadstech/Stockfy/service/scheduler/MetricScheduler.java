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

  @Scheduled(cron = "0 0 0 * * *")
  public void processMetrics() {
    processMetricsForDate(LocalDate.now());
  }

  public void processMetricsForDate(LocalDate today) {
    log.info("Processing metrics for {}", today);
    
    // Persist every day (for the previous day)
    metricDailyService.calculateAndSave(today.minusDays(1));

    // Persist monthly only if current day is first day of month (for the previous month)
    if (today.getDayOfMonth() == 1) {
      metricMonthlyService.calculateAndSave(today.minusMonths(1));
    }

    // Persist yearly only if current day is first day of year (for the previous year)
    if (today.getDayOfYear() == 1) {
      metricYearlyService.calculateAndSave(today.minusYears(1));
    }
  }
}
