package br.com.threadstech.stockfy.entity;

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

import br.com.threadstech.stockfy.enums.ProductType;
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
@Table(name = "products")
@ToString
public class Product {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(nullable = false)
  private Long id;

  @Column(name = "bar_code", unique = true, nullable = false, length = 20)
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

  @Column(name = "available", nullable = false)
  boolean available = true;

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
