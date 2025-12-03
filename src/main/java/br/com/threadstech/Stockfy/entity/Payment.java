package br.com.threadstech.stockfy.entity;

import br.com.threadstech.stockfy.enums.PaymentStatus;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "payments")
@ToString
public class Payment {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(nullable = false)
  private Long id;

  @Embedded
  @Enumerated(EnumType.STRING)
  @Column(name = "payment_method", nullable = false, length = 20)
  private Cart paymentMethod;

  @Enumerated(EnumType.STRING)
  @Column(name = "payment_status", nullable = false, length = 20)
  private PaymentStatus paymentStatus;

  @Column(name = "payment_date")
  private Instant paymentDate;

  @ElementCollection
  @CollectionTable(name = "payment_products", joinColumns = @JoinColumn(name = "payment_id"))
  private List<Cart> carts;

  @Column(name = "total", precision = 10, scale = 2)
  private BigDecimal total;
}
