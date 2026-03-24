package br.com.threadstech.stockfy.modules.product;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.com.threadstech.stockfy.PostgreTestContainer;
import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Import(PostgreTestContainer.class)
class ProductRepositoryTest {

  @Autowired private ProductRepository productRepository;

  @Test
  @DisplayName("shouldSaveProductWhenBarcodeIsUnique")
  void shouldSaveProductWhenBarcodeIsUnique() {
    Product product =
        Product.builder()
            .barcode(ProductTestData.BARCODE)
            .name(ProductTestData.NAME)
            .stock(ProductTestData.STOCK)
            .minStock(ProductTestData.MIN_STOCK)
            .price(ProductTestData.PRICE)
            .cost(ProductTestData.COST)
            .profit(new BigDecimal("50.00"))
            .discount(ProductTestData.DISCOUNT)
            .type(ProductTestData.TYPE)
            .deleted(false)
            .build();

    Product savedProduct = productRepository.save(product);

    assertThat(savedProduct.getId()).isNotNull();
    assertThat(savedProduct.getBarcode()).isEqualTo(ProductTestData.BARCODE);
  }

  @Test
  @DisplayName("shouldThrowExceptionWhenBarcodeExists")
  void shouldThrowExceptionWhenBarcodeExists() {
    Product product1 =
        Product.builder()
            .barcode("DUPLICATE")
            .name("Product 1")
            .stock(10)
            .minStock(5)
            .price(new BigDecimal("100.00"))
            .cost(new BigDecimal("50.00"))
            .profit(new BigDecimal("50.00"))
            .type("ELECTRONICS")
            .deleted(false)
            .build();
    productRepository.save(product1);

    Product product2 =
        Product.builder()
            .barcode("DUPLICATE")
            .name("Product 2")
            .stock(10)
            .minStock(5)
            .price(new BigDecimal("100.00"))
            .cost(new BigDecimal("50.00"))
            .profit(new BigDecimal("50.00"))
            .type("ELECTRONICS")
            .deleted(false)
            .build();

    assertThatThrownBy(() -> productRepository.saveAndFlush(product2))
        .isInstanceOf(DataIntegrityViolationException.class);
  }
}
