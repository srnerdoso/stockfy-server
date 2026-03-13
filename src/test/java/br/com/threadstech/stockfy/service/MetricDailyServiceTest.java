package br.com.threadstech.stockfy.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import br.com.threadstech.stockfy.entity.MetricDaily;
import br.com.threadstech.stockfy.enums.PaymentStatus;
import br.com.threadstech.stockfy.repository.CustomerRepository;
import br.com.threadstech.stockfy.repository.MetricDailyRepository;
import br.com.threadstech.stockfy.repository.PaymentRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MetricDailyServiceTest {

  @Mock private MetricDailyRepository metricDailyRepository;
  @Mock private PaymentRepository paymentRepository;
  @Mock private CustomerRepository customerRepository;

  @InjectMocks private MetricDailyService metricDailyService;

  @Test
  @DisplayName("Should calculate and save daily metrics correctly")
  void shouldCalculateAndSaveDailyMetrics() {
    LocalDate targetDate = LocalDate.now().minusDays(1);
    Instant start = targetDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
    Instant end = targetDate.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant();

    when(customerRepository.countByCreatedAtBetween(start, end)).thenReturn(5L);
    when(paymentRepository.countByPaymentStatusAndCreatedAtBetween(PaymentStatus.PAID, start, end)).thenReturn(10L);
    // Let's assume a custom query or method in PaymentRepository for profit calculation
    when(paymentRepository.calculateProfitByStatusAndCreatedAtBetween(PaymentStatus.PAID, start, end))
        .thenReturn(new BigDecimal("150.00"));

    metricDailyService.calculateAndSave(targetDate);

    ArgumentCaptor<MetricDaily> captor = ArgumentCaptor.forClass(MetricDaily.class);
    verify(metricDailyRepository).save(captor.capture());

    MetricDaily saved = captor.getValue();
    assertThat(saved.getDate()).isEqualTo(targetDate);
    assertThat(saved.getNewCustomersCount()).isEqualTo(5L);
    assertThat(saved.getPurchasesCount()).isEqualTo(10L);
    assertThat(saved.getProfit()).isEqualByComparingTo("150.00");
    assertThat(saved.getExpenses()).isEqualByComparingTo(BigDecimal.ZERO);
  }
}
