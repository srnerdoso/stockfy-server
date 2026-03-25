package br.com.threadstech.stockfy.modules.product;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductV1_1Repository extends JpaRepository<ProductV1_1, Long> {
  Optional<ProductV1_1> findByBarcode(String barcode);
}
