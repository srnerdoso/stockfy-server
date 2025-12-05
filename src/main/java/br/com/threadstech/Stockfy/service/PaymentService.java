package br.com.threadstech.stockfy.service;

import br.com.threadstech.stockfy.entity.Cart;
import br.com.threadstech.stockfy.entity.Customer;
import br.com.threadstech.stockfy.entity.Payment;
import br.com.threadstech.stockfy.entity.Product;
import br.com.threadstech.stockfy.enums.PaymentStatus;
import br.com.threadstech.stockfy.repository.CustomerRepository;
import br.com.threadstech.stockfy.repository.PaymentRepository;
import br.com.threadstech.stockfy.repository.ProductRepository;
import br.com.threadstech.stockfy.web.dto.CartCreateDto;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

  private final PaymentRepository paymentRepository;
  private final ProductRepository productRepository;

  @Transactional
  public void save(Payment payment) {
    for (Cart cart : payment.getCart()) {
      Product product = cart.getProduct();
      BigDecimal productStock = product.getStock();
      BigDecimal quantity = cart.getQuantity();
      product.setStock(productStock.subtract(quantity));
    }
    paymentRepository.save(payment);
    log.info("Payment successfully: {}", payment);
  }

  @Transactional(readOnly = true)
  public Set<Product> getProductsFromCarts(Set<CartCreateDto> carts) {
    List<Long> ids = carts.stream().map(CartCreateDto::getProductId).toList();
    return new HashSet<>(productRepository.findAllById(ids));
  }
}
