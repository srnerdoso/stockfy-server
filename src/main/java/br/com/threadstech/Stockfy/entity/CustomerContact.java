package br.com.threadstech.stockfy.entity;

import br.com.threadstech.stockfy.config.constraints.CustomerConstraintNames;
import br.com.threadstech.stockfy.entity.base.BaseContact;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.Set;

import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(
    name = "customer_contacts",
    uniqueConstraints = {
      @UniqueConstraint(name = CustomerConstraintNames.UK_EMAIL, columnNames = "email"),
      @UniqueConstraint(
          name = CustomerConstraintNames.UK_PHONE_NUMBER,
          columnNames = "phone_number")
    })
@SQLDelete(
    sql =
        """
    UPDATE customer_contacts
    SET deleted = true,
        email = CONCAT('email_', id, '_deleted'),
        phone_number = CONCAT('email_', id, '_deleted')
    WHERE id = ?
    """)
public class CustomerContact extends BaseContact {

  @OneToMany(mappedBy = "contact")
  private Set<Customer> customers;
}
