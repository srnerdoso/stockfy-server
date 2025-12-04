package br.com.threadstech.stockfy.repository;

import br.com.threadstech.stockfy.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {}
