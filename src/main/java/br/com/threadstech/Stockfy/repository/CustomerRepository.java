package br.com.threadstech.stockfy.repository;

import br.com.threadstech.stockfy.entity.Customer;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

  @Query("SELECT c.cpf FROM Customer c WHERE c.id = :id")
  Optional<String> findCpfById(Long id);
}
