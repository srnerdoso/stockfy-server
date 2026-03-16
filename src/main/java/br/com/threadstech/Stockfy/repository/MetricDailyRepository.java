package br.com.threadstech.stockfy.repository;

import br.com.threadstech.stockfy.entity.MetricDaily;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MetricDailyRepository extends JpaRepository<MetricDaily, Long> {
  Optional<MetricDaily> findByDate(LocalDate date);

  List<MetricDaily> findAllByDateBetweenOrderByDateAsc(LocalDate start, LocalDate end);
}
