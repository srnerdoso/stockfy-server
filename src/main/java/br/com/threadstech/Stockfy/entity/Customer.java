package br.com.threadstech.stockfy.entity;

import br.com.threadstech.stockfy.config.constraints.CustomerConstraintNames;
import br.com.threadstech.stockfy.entity.base.BaseAudit;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDate;
import java.util.Objects;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(
    name = "customers",
    uniqueConstraints = {
      @UniqueConstraint(name = CustomerConstraintNames.UK_CPF, columnNames = "cpf")
    })
@SQLDelete(
    sql =
        """
    UPDATE customers
    SET deleted = true,
        full_name = 'deleted',
        cpf = CONCAT('cpf_', id, '_deleted')
    WHERE id = ?
    """)
@SQLRestriction("deleted = false")
public class Customer extends BaseAudit {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(nullable = false)
  private Long id;

  @Column(name = "full_name", nullable = false, length = 255)
  private String fullName;

  @Column(name = "cpf", nullable = false, unique = true, length = 20)
  private String cpf;

  @Column(name = "birthday", nullable = false)
  private LocalDate birthday;

  @ManyToOne(cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)
  @JoinColumn(name = "customer_contact_id", nullable = false)
  private CustomerContact contact;

  @ManyToOne(cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)
  @JoinColumn(name = "customer_address_id", nullable = false)
  private CustomerAddress address;

  @OneToMany(mappedBy = "customer")
  private Set<Payment> payments;

  @Column(name = "deleted", nullable = false)
  private boolean deleted = false;

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Customer customer = (Customer) o;
    return Objects.equals(id, customer.id);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(id);
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + "(" + "id = " + id + ")";
  }
}
