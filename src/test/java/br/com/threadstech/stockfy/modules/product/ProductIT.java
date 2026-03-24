package br.com.threadstech.stockfy.modules.product;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.threadstech.stockfy.annotations.AdminTest;
import br.com.threadstech.stockfy.annotations.IntegrationTests;
import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@IntegrationTests
class ProductIT {

  @Autowired private MockMvc mockMvc;

  @Autowired private ProductRepository productRepository;

  private static final String PRODUCT_V1_1 = "/api/v1_1/products";

  @AdminTest
  @DisplayName("shouldCompleteProductCreationFlow")
  void shouldCompleteProductCreationFlow() throws Exception {
    mockMvc
        .perform(
            post(PRODUCT_V1_1)
                .contentType(MediaType.APPLICATION_JSON)
                .content(ProductTestData.VALID_CREATE_JSON))
        .andExpect(status().isCreated());

    Product savedProduct =
        productRepository.findByBarcode(ProductTestData.BARCODE).orElseThrow();

    assertThat(savedProduct.getName()).isEqualTo(ProductTestData.NAME);
    assertThat(savedProduct.getStock()).isEqualTo(ProductTestData.STOCK);
    assertThat(savedProduct.getPrice()).isEqualByComparingTo(ProductTestData.PRICE);
    assertThat(savedProduct.getCost()).isEqualByComparingTo(ProductTestData.COST);
    assertThat(savedProduct.getProfit()).isEqualByComparingTo(new BigDecimal("50.00"));
    assertThat(savedProduct.getDeleted()).isFalse();
  }

  @AdminTest
  @DisplayName("shouldFailCreationWhenDomainRulesAreViolated")
  void shouldFailCreationWhenDomainRulesAreViolated() throws Exception {
    // Price < Cost
    mockMvc
        .perform(
            post(PRODUCT_V1_1)
                .contentType(MediaType.APPLICATION_JSON)
                .content(ProductTestData.INVALID_JSON_PRICE_LESS_THAN_COST))
        .andExpect(status().isBadRequest());

    // Discount > Price
    mockMvc
        .perform(
            post(PRODUCT_V1_1)
                .contentType(MediaType.APPLICATION_JSON)
                .content(ProductTestData.INVALID_JSON_DISCOUNT_GREATER_THAN_PRICE))
        .andExpect(status().isBadRequest());
  }
}
