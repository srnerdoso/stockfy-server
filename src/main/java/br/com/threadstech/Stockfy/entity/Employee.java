package br.com.threadstech.stockfy.entity;

import br.com.threadstech.stockfy.config.constraints.EmployeeConstraintNames;
import br.com.threadstech.stockfy.entity.base.BaseAudit;
import br.com.threadstech.stockfy.enums.Role;
import br.com.threadstech.stockfy.security.refreshtoken.RefreshToken;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.Objects;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.envers.AuditOverride;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;
import org.hibernate.envers.RelationTargetAuditMode;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Audited
@Table(
    name = "employees",
    uniqueConstraints = {
      @UniqueConstraint(
          name = EmployeeConstraintNames.UK_CPF,
          columnNames = {"cpf"})
    })
public class Employee extends BaseAudit {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(nullable = false)
  private Long id;

  @NotAudited
  @Column(name = "cpf", unique = true, nullable = false, length = 255)
  private String cpf;

  @Column(name = "birthday", nullable = false)
  private LocalDate birthday;

  @Column(name = "full_name", nullable = false, length = 255)
  @NotAudited
  private String fullName;

  @Enumerated(EnumType.STRING)
  @Column(name = "role", nullable = false, length = 25)
  private Role role;

  @NotAudited
  @Column(name = "password", nullable = false, length = 255)
  private String password;

  @ManyToOne(cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)
  @JoinColumn(name = "employee_contact_id")
  @NotAudited
  private EmployeeContact contact;

  @ManyToOne(cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)
  @JoinColumn(name = "employee_address_id")
  @NotAudited
  private EmployeeAddress address;

  @OneToMany(
      cascade = CascadeType.PERSIST,
      mappedBy = "employee",
      fetch = FetchType.LAZY,
      orphanRemoval = true)
  @NotAudited
  private Set<RefreshToken> refreshTokens;

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
