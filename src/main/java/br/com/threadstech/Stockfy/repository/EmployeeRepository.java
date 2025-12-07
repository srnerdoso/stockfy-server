package br.com.threadstech.stockfy.repository;

import br.com.threadstech.stockfy.entity.Employee;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {

  @Query("SELECT e.cpf FROM Employee e WHERE e.id = :id")
  Optional<String> findCpfById(Long id);

  @Query("SELECT e.password FROM Employee e WHERE e.id = :id")
  Optional<String> findPasswordById(Long id);

  @Modifying
  @Query("UPDATE Employee e SET e.password = :newPassword WHERE e.id = :id")
  void updatePasswordById(Long id, String newPassword);
}
