package br.com.threadstech.stockfy.modules.product;

import br.com.threadstech.stockfy.core.enums.UnitType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(
    name = "products_v1_1",
    uniqueConstraints = {
      @UniqueConstraint(name = ProductV1_1ConstraintNames.UK_BARCODE, columnNames = "barcode")
    })
public class ProductV1_1 {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(nullable = false)
  private Long id;

  @Column(name = "name", nullable = false, length = 255, updatable = false)
  private String name;

  @Column(name = "barcode", nullable = false)
  private String barcode;

  @Column(name = "stock_quantity", nullable = false, precision = 10, scale = 3)
  private BigDecimal stockQuantity;

  @Builder.Default
  @Column(name = "minimum_stock", nullable = false, precision = 10, scale = 3)
  private BigDecimal minimumStock = BigDecimal.ZERO;

  @Column(name = "price", nullable = false, precision = 10, scale = 2)
  private BigDecimal price;

  @Column(name = "cost", nullable = false, precision = 10, scale = 2)
  private BigDecimal cost;

  @Column(name = "discount", precision = 10, scale = 2)
  private BigDecimal discount;

  @Enumerated(EnumType.STRING)
  @Column(name = "unit_type", nullable = false, length = 20)
  private UnitType unitType;

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    ProductV1_1 product = (ProductV1_1) o;
    return Objects.equals(id, product.id);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(id);
  }
}
