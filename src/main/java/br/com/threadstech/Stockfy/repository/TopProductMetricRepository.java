package br.com.threadstech.stockfy.repository;

import br.com.threadstech.stockfy.entity.TopProductMetric;
import br.com.threadstech.stockfy.enums.ProductType;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface TopProductMetricRepository extends JpaRepository<TopProductMetric, Long> {
  List<TopProductMetric> findAllByProductTypeAndDateOrderBySalesCountDesc(ProductType productType, LocalDate date);

  @Query("SELECT MAX(date) FROM TopProductMetric")
  Optional<LocalDate> findMaxDate();
}
