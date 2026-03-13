package br.com.threadstech.stockfy.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import br.com.threadstech.stockfy.entity.MetricMonthly;
import br.com.threadstech.stockfy.enums.PaymentStatus;
import br.com.threadstech.stockfy.repository.CustomerRepository;
import br.com.threadstech.stockfy.repository.MetricMonthlyRepository;
import br.com.threadstech.stockfy.repository.PaymentRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MetricMonthlyServiceTest {

  @Mock private MetricMonthlyRepository metricMonthlyRepository;
  @Mock private PaymentRepository paymentRepository;
  @Mock private CustomerRepository customerRepository;

  @InjectMocks private MetricMonthlyService metricMonthlyService;

  @Test
  @DisplayName("Should calculate and save monthly metrics correctly")
  void shouldCalculateAndSaveMonthlyMetrics() {
    LocalDate targetMonth = LocalDate.now().minusMonths(1).withDayOfMonth(1);
    Instant start = targetMonth.atStartOfDay(ZoneId.systemDefault()).toInstant();
    Instant end = targetMonth.plusMonths(1).atStartOfDay(ZoneId.systemDefault()).toInstant();

    when(customerRepository.countByCreatedAtBetween(start, end)).thenReturn(100L);
    when(paymentRepository.countByPaymentStatusAndCreatedAtBetween(PaymentStatus.PAID, start, end)).thenReturn(500L);
    when(paymentRepository.calculateProfitByStatusAndCreatedAtBetween(PaymentStatus.PAID, start, end))
        .thenReturn(new BigDecimal("15000.00"));

    metricMonthlyService.calculateAndSave(targetMonth);

    ArgumentCaptor<MetricMonthly> captor = ArgumentCaptor.forClass(MetricMonthly.class);
    verify(metricMonthlyRepository).save(captor.capture());

    MetricMonthly saved = captor.getValue();
    assertThat(saved.getDate()).isEqualTo(targetMonth);
    assertThat(saved.getNewCustomersCount()).isEqualTo(100L);
    assertThat(saved.getPurchasesCount()).isEqualTo(500L);
    assertThat(saved.getProfit()).isEqualByComparingTo("15000.00");
  }
}
