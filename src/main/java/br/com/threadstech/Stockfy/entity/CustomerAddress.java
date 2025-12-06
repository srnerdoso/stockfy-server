package br.com.threadstech.stockfy.entity;

import br.com.threadstech.stockfy.entity.base.BaseAddress;
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
import org.hibernate.annotations.SQLDelete;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "customer_addresses")
@SQLDelete(
    sql =
        """
    UPDATE customer_addresses
    SET deleted = true,
        street = 'deleted',
        number = 'deleted',
        complement = 'deleted',
        neighborhood = 'deleted',
        city = 'deleted',
        state = 'deleted',
        zip_code = 'deleted',
        country = 'deleted'
    WHERE id = ?
    """)
public class CustomerAddress extends BaseAddress {

  @OneToMany(mappedBy = "address")
  private Set<Customer> customers;
}
