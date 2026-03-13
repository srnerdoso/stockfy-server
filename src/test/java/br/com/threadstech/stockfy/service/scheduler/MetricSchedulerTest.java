package br.com.threadstech.stockfy.service.scheduler;

import static org.mockito.Mockito.*;

import br.com.threadstech.stockfy.service.MetricDailyService;
import br.com.threadstech.stockfy.service.MetricMonthlyService;
import br.com.threadstech.stockfy.service.MetricYearlyService;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MetricSchedulerTest {

  @Mock private MetricDailyService metricDailyService;
  @Mock private MetricMonthlyService metricMonthlyService;
  @Mock private MetricYearlyService metricYearlyService;

  @InjectMocks private MetricScheduler metricScheduler;

  @Test
  @DisplayName("Should call daily service every day")
  void shouldCallDailyServiceEveryDay() {
    metricScheduler.processMetrics();
    verify(metricDailyService).calculateAndSave(any(LocalDate.class));
  }

  @Test
  @DisplayName("Should call monthly service on the first day of the month")
  void shouldCallMonthlyServiceOnFirstDayOfMonth() {
    // We need to inject a mock clock or mock LocalDate.now()
    // For simplicity let's assume MetricScheduler has a processMetrics(LocalDate today) method
    // or we use a Clock.
    
    LocalDate firstDayOfMonth = LocalDate.of(2026, 3, 1);
    metricScheduler.processMetricsForDate(firstDayOfMonth);
    
    verify(metricDailyService).calculateAndSave(firstDayOfMonth.minusDays(1));
    verify(metricMonthlyService).calculateAndSave(firstDayOfMonth.minusMonths(1).withDayOfMonth(1));
  }

  @Test
  @DisplayName("Should call yearly service on the first day of the year")
  void shouldCallYearlyServiceOnFirstDayOfYear() {
    LocalDate firstDayOfYear = LocalDate.of(2026, 1, 1);
    metricScheduler.processMetricsForDate(firstDayOfYear);
    
    verify(metricDailyService).calculateAndSave(firstDayOfYear.minusDays(1));
    verify(metricMonthlyService).calculateAndSave(firstDayOfYear.minusMonths(1).withDayOfMonth(1));
    verify(metricYearlyService).calculateAndSave(firstDayOfYear.minusYears(1).withDayOfYear(1));
  }
}
