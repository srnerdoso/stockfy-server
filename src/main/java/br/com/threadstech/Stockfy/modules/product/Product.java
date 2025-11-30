package br.com.threadstech.stockfy.modules.product;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

/**
 * Representa um produto do sistema Stockfy.
 *
 * <p>Cada produto possui informações de identificação, estoque, preço, custo, lucro, descontos e
 * tipo de produto.
 *
 * <p>Os campos desta entidade são mapeados para a tabela "products" no banco de dados.
 *
 * <p>Exemplo de uso:
 *
 * <pre>{@code
 * Product product = new Product();
 * product.setName("Arroz");
 * product.setBarCode("1234567890123");
 * product.setStock(new BigDecimal("100.000"));
 * product.setPrice(new BigDecimal("12.50"));
 * product.setCost(new BigDecimal("8.00"));
 * product.setProfit(new BigDecimal("4.50"));
 * product.setDiscount(new BigDecimal("0.00"));
 * product.setDiscountPercentage(new BigDecimal("0.00"));
 * product.setType(ProductType.UNIT);
 * }</pre>
 *
 * @author threadstech
 * @see ProductType
 */
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

  @Column(name = "type", nullable = false, length = 20)
  private ProductType type;

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Product stock = (Product) o;
    return Objects.equals(id, stock.id);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(id);
  }
}
