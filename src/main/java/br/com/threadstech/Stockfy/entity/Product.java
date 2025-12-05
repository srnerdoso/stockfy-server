package br.com.threadstech.stockfy.entity;

import br.com.threadstech.stockfy.enums.ProductType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "products")
@SQLDelete(
    sql =
        """
    UPDATE products SET deleted = true,
      bar_code = CONCAT(bar_code, '_deleted_', id)
    WHERE id = ?
    """)
@SQLRestriction("deleted = false")
public class Product {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(nullable = false)
  private Long id;

  @Column(name = "bar_code", unique = true, nullable = false, length = 30)
  private String barCode;

  @Column(name = "name", nullable = false, length = 255)
  private String name;

  @Column(name = "stock", nullable = false, precision = 10, scale = 3)
  private BigDecimal stock;

  @Column(name = "price", nullable = false, precision = 10, scale = 2)
  private BigDecimal price;

  @Column(name = "cost", nullable = false, precision = 10, scale = 2)
  private BigDecimal cost;

  @Column(name = "profit", nullable = false, precision = 10, scale = 2)
  private BigDecimal profit;

  @Column(name = "discount", nullable = false, precision = 10, scale = 2)
  private BigDecimal discount;

  @Column(name = "discount_percentage", nullable = false, precision = 5, scale = 2)
  private BigDecimal discountPercentage;

  @Enumerated(EnumType.STRING)
  @Column(name = "type", nullable = false, length = 20)
  private ProductType type;

  @Column(name = "deleted", nullable = false)
  boolean deleted = false;

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Product product = (Product) o;
    return Objects.equals(id, product.id);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(id);
  }
}
