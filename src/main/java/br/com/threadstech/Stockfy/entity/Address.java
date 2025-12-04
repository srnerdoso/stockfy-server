package br.com.threadstech.stockfy.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "addresses")
@ToString
public class Address {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(nullable = false)
  private Long id;

  @Column(name = "street", nullable = false, length = 255)
  private String street;

  @Column(name = "number", nullable = false, length = 20)
  private String number;

  @Column(name = "complement", nullable = false, length = 255)
  private String complement;

  @Column(name = "neighborhood", nullable = false, length = 255)
  private String neighborhood;

  @Column(name = "city", nullable = false, length = 255)
  private String city;

  @Column(name = "state", nullable = false, length = 65)
  private String state;

  @Column(name = "zip_code", nullable = false, length = 20)
  private String zipCode;

  @Column(name = "country", nullable = false, length = 100)
  private String country;

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Address address = (Address) o;
    return Objects.equals(id, address.id);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(id);
  }
}
