package br.com.threadstech.stockfy.entity;

import br.com.threadstech.stockfy.entity.base.BaseContact;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.Set;
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
@Table(name = "customer_contacts")
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
