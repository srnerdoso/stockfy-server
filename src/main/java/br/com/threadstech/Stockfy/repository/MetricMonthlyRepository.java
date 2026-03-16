package br.com.threadstech.stockfy.repository;

import br.com.threadstech.stockfy.entity.MetricMonthly;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MetricMonthlyRepository extends JpaRepository<MetricMonthly, Long> {
  Optional<MetricMonthly> findByDate(LocalDate date);

  List<MetricMonthly> findAllByDateBetweenOrderByDateAsc(LocalDate start, LocalDate end);
}
