package br.com.threadstech.stockfy.repository;

import br.com.threadstech.stockfy.entity.MetricMonthly;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MetricMonthlyRepository extends JpaRepository<MetricMonthly, Long> {}
