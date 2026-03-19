package br.com.threadstech.stockfy.repository;

import br.com.threadstech.stockfy.entity.Alert;
import br.com.threadstech.stockfy.entity.Product;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AlertRepository extends JpaRepository<Alert, Long> {
  Optional<Alert> findByProduct(Product product);
  void deleteByProduct(Product product);
}
