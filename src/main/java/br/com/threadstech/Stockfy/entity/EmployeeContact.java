package br.com.threadstech.stockfy.entity;

import br.com.threadstech.stockfy.entity.base.BaseContact;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.Objects;
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
@Table(name = "employee_contacts")
public class EmployeeContact extends BaseContact {

  @OneToMany(mappedBy = "contact")
  private Set<Employee> employees;
}
