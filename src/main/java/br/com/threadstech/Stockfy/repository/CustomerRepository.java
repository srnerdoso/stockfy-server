package br.com.threadstech.stockfy.repository;

import br.com.threadstech.stockfy.entity.Customer;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

  Optional<Customer> findByFullName(String fullName);
}
