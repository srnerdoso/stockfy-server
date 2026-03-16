package br.com.threadstech.stockfy.repository;

import br.com.threadstech.stockfy.entity.MetricYearly;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MetricYearlyRepository extends JpaRepository<MetricYearly, Long> {
  Optional<MetricYearly> findByDate(LocalDate date);

  List<MetricYearly> findAllByDateBetweenOrderByDateAsc(LocalDate start, LocalDate end);
}
