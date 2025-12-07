package br.com.threadstech.stockfy.entity;

import br.com.threadstech.stockfy.entity.base.BaseAddress;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "employee_addresses")
public class EmployeeAddress extends BaseAddress {

  @OneToMany(mappedBy = "address")
  private Set<Employee> employees;
}
