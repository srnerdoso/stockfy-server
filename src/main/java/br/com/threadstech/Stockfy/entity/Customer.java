package br.com.threadstech.stockfy.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDate;
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
@Table(name = "customers")
public class Customer extends BaseAudit {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(nullable = false)
  private Long id;

  @Column(name = "full_name", nullable = false, length = 255)
  private String fullName;

  @Column(name = "cpf", nullable = false, unique = true, length = 255)
  private String cpf;

  @Column(name = "birthday", nullable = false)
  private LocalDate birthday;

  @ManyToOne(cascade = CascadeType.PERSIST)
  @JoinColumn(name = "contact_id", nullable = false)
  private Contact contact;

  @ManyToOne(cascade = CascadeType.PERSIST)
  @JoinColumn(name = "address_id", nullable = false)
  private Address address;

  @OneToMany(mappedBy = "customer")
  private Set<Payment> payments;

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
