package br.com.threadstech.stockfy.entity;

import br.com.threadstech.stockfy.enums.Role;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "addresses")
public class Employee extends BaseAudit {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(nullable = false)
  private Long id;

  @Column(name = "cpf", unique = true, nullable = false, length = 255)
  private String cpf;

  @Column(name = "name", nullable = false, length = 255)
  private String name;

  @Enumerated(EnumType.STRING)
  @Column(name = "role", nullable = false, length = 25)
  private Role role;

  @OneToOne
  @JoinColumn(name = "contact_id")
  private Contact contact;

  @Column(name = "password", nullable = false, length = 255)
  private String password;

  @OneToOne
  @JoinColumn(name = "address_id")
  private Address address;

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Employee employee = (Employee) o;
    return Objects.equals(id, employee.id);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(id);
  }

  @Override
  public String toString() {
    return "Employee{" + "id=" + id + '}';
  }
}
