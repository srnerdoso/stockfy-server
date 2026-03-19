package br.com.threadstech.stockfy.repository;

import br.com.threadstech.stockfy.entity.Payment;
import br.com.threadstech.stockfy.enums.PaymentStatus;
import br.com.threadstech.stockfy.enums.ProductType;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PaymentRepository extends JpaRepository<Payment, Long> {


  @Query(
      """
      SELECT p FROM Payment p
      LEFT JOIN FETCH p.cart c
      LEFT JOIN FETCH c.product
      WHERE p.paymentStatus = :status
      """)
  List<Payment> findAllByPaymentStatus(@Param("status") PaymentStatus status);

  long countByPaymentStatusAndCreatedAtBetween(PaymentStatus status, Instant start, Instant end);

  @Query(
      """
      SELECT SUM(p.total)
      FROM Payment p
      WHERE p.paymentStatus = :status AND p.createdAt BETWEEN :start AND :end
      """)
  BigDecimal calculateTotalSalesByStatusAndCreatedAtBetween(
      @Param("status") PaymentStatus status,
      @Param("start") Instant start,
      @Param("end") Instant end);

  @Query(
      """
      SELECT SUM(c.paymentValue - (c.product.cost * c.quantity))
      FROM Payment p
      JOIN p.cart c
      WHERE p.paymentStatus = :status AND p.createdAt BETWEEN :start AND :end
      """)
  BigDecimal calculateProfitByStatusAndCreatedAtBetween(
      @Param("status") PaymentStatus status,
      @Param("start") Instant start,
      @Param("end") Instant end);

  @Query(
      """
      SELECT p FROM Payment p
      LEFT JOIN FETCH p.customer
      ORDER BY p.createdAt DESC
      """)
  List<Payment> findLastSales(Pageable pageable);
}
