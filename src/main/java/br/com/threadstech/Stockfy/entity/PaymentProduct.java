package br.com.threadstech.stockfy.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.math.BigDecimal;

@Embeddable
public class PaymentProduct {
  
  @Column(name = "payment_id", nullable = false)
  private Payment payment;

  @Column(name = "product_id", nullable = false)
  private Product product;

  @Column(name = "quantity", nullable = false, precision = 10, scale = 3)
  private BigDecimal quantity;
}
