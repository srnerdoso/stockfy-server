package br.com.threadstech.stockfy.service;

import br.com.threadstech.stockfy.entity.Alert;
import br.com.threadstech.stockfy.entity.Product;
import br.com.threadstech.stockfy.repository.AlertRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AlertService {

  private final AlertRepository alertRepository;

  @Transactional(readOnly = true)
  public List<Alert> findAll() {
    return alertRepository.findAll();
  }

  @Transactional
  public void processProductStock(Product product) {
    if (product.getStock().compareTo(product.getMinStock()) < 0) {
      if (alertRepository.findByProduct(product).isEmpty()) {
        alertRepository.save(
            Alert.builder()
                .type("low-stock")
                .product(product)
                .build());
      }
    } else {
      alertRepository.deleteByProduct(product);
    }
  }
}
