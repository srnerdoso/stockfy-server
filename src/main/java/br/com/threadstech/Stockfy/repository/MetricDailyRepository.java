package br.com.threadstech.stockfy.repository;

import br.com.threadstech.stockfy.entity.MetricDaily;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MetricDailyRepository extends JpaRepository<MetricDaily, Long> {}
