package br.com.threadstech.stockfy.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import br.com.threadstech.stockfy.entity.MetricYearly;
import br.com.threadstech.stockfy.enums.PaymentStatus;
import br.com.threadstech.stockfy.repository.CustomerRepository;
import br.com.threadstech.stockfy.repository.MetricYearlyRepository;
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
class MetricYearlyServiceTest {

  @Mock private MetricYearlyRepository metricYearlyRepository;
  @Mock private PaymentRepository paymentRepository;
  @Mock private CustomerRepository customerRepository;

  @InjectMocks private MetricYearlyService metricYearlyService;

  @Test
  @DisplayName("Should calculate and save yearly metrics correctly")
  void shouldCalculateAndSaveYearlyMetrics() {
    LocalDate targetYear = LocalDate.now().minusYears(1).withDayOfYear(1);
    Instant start = targetYear.atStartOfDay(ZoneId.systemDefault()).toInstant();
    Instant end = targetYear.plusYears(1).atStartOfDay(ZoneId.systemDefault()).toInstant();

    when(customerRepository.countByCreatedAtBetween(start, end)).thenReturn(1000L);
    when(paymentRepository.countByPaymentStatusAndCreatedAtBetween(PaymentStatus.PAID, start, end)).thenReturn(5000L);
    when(paymentRepository.calculateProfitByStatusAndCreatedAtBetween(PaymentStatus.PAID, start, end))
        .thenReturn(new BigDecimal("250000.00"));

    metricYearlyService.calculateAndSave(targetYear);

    ArgumentCaptor<MetricYearly> captor = ArgumentCaptor.forClass(MetricYearly.class);
    verify(metricYearlyRepository).save(captor.capture());

    MetricYearly saved = captor.getValue();
    assertThat(saved.getDate()).isEqualTo(targetYear);
    assertThat(saved.getNewCustomersCount()).isEqualTo(1000L);
    assertThat(saved.getPurchasesCount()).isEqualTo(5000L);
    assertThat(saved.getProfit()).isEqualByComparingTo("250000.00");
  }
}
