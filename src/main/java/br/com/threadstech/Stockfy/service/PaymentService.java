package br.com.threadstech.stockfy.service;

import br.com.threadstech.stockfy.config.properties.PaymentPropertiesConfig;
import br.com.threadstech.stockfy.entity.Cart;
import br.com.threadstech.stockfy.entity.Payment;
import br.com.threadstech.stockfy.entity.Product;
import br.com.threadstech.stockfy.enums.PaymentStatus;
import br.com.threadstech.stockfy.exception.EntityNotFoundException;
import br.com.threadstech.stockfy.exception.UnavailableFromRefundException;
import br.com.threadstech.stockfy.repository.PaymentRepository;
import br.com.threadstech.stockfy.repository.ProductRepository;
import br.com.threadstech.stockfy.web.dto.CartCreateDto;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@ConfigurationProperties(prefix = "payment-service")
public class PaymentService {

  private final PaymentRepository paymentRepository;
  private final ProductRepository productRepository;
  private final PaymentPropertiesConfig properties;

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

  @Transactional(readOnly = true)
  public Payment findById(Long id) {
    return paymentRepository
        .findById(id)
        .orElseThrow(() -> new EntityNotFoundException(id.toString()));
  }

  @Transactional
  public void refund(Long id) {
    Payment payment = findById(id);
    verifyPayment(payment);
    for (Cart cart : payment.getCart()) {
      Product product = cart.getProduct();
      BigDecimal productStock = product.getStock();
      BigDecimal quantity = cart.getQuantity();
      product.setStock(productStock.add(quantity));
    }
    payment.setPaymentStatus(PaymentStatus.REFUNDED);
    log.info("Payment refunded successfully: {}", payment);
    log.info("Payment refunded successfully: {}", payment.getPaymentStatus());
  }

  @Transactional(readOnly = true)
  public List<Payment> findAllByStatus(PaymentStatus status) {
    return paymentRepository.findAllByPaymentStatus(status);
  }

  private void verifyPayment(Payment payment) {
    Long paymentId = payment.getId();
    long daysSincePayment = Duration.between(payment.getCreatedAt(), Instant.now()).toDays();
    boolean isDaysSincePaymentGreaterThanRefundMaxDays =
        daysSincePayment > properties.getRefundMaxDays();
    boolean isPaymentNotPaid = payment.getPaymentStatus() != PaymentStatus.PAID;

    if (isDaysSincePaymentGreaterThanRefundMaxDays || isPaymentNotPaid) {
      log.info(
          """
          Payment id={} is invalid:
            isDaysSincePaymentGreaterThanRefundMaxDays={};
            isPaymentNotPaid={}.
          """,
          paymentId,
          isDaysSincePaymentGreaterThanRefundMaxDays,
          isPaymentNotPaid);
      throw new UnavailableFromRefundException(payment.getId());
    }
  }
}
