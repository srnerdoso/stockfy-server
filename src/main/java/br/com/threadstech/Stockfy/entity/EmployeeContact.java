package br.com.threadstech.stockfy.entity;

import br.com.threadstech.stockfy.config.constraints.EmployeeConstraintNames;
import br.com.threadstech.stockfy.entity.base.BaseContact;
import jakarta.persistence.*;

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
@Table(
    name = "employee_contacts",
    uniqueConstraints = {
      @UniqueConstraint(name = EmployeeConstraintNames.UK_EMAIL, columnNames = "email"),
      @UniqueConstraint(
          name = EmployeeConstraintNames.UK_PHONE_NUMBER,
          columnNames = "phone_number")
    })
public class EmployeeContact extends BaseContact {

  @OneToMany(mappedBy = "contact")
  private Set<Employee> employees;
}
