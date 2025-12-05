package br.com.threadstech.stockfy.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.ManyToOne;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Embeddable
public class Cart {

  @ManyToOne private Product product;

  @Column(name = "quantity", nullable = false, precision = 10, scale = 3)
  private BigDecimal quantity;

  @Column(name = "payment_value", nullable = false, precision = 10, scale = 2)
  private BigDecimal paymentValue;
}
