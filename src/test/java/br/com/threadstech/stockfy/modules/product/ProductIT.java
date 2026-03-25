package br.com.threadstech.stockfy.modules.product;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.threadstech.stockfy.annotations.AdminTest;
import br.com.threadstech.stockfy.annotations.IntegrationTests;
import br.com.threadstech.stockfy.annotations.InventoryManagerTest;
import br.com.threadstech.stockfy.annotations.SalesAttendantTest;
import br.com.threadstech.stockfy.api.ApiPaths;
import br.com.threadstech.stockfy.core.enums.UnitType;
import br.com.threadstech.stockfy.modules.product.dto.ProductCreateDto;
import br.com.threadstech.stockfy.utils.DataGenUtils;
import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@IntegrationTests
public class ProductIT {

  @Autowired private MockMvc mockMvc;
  @Autowired private ProductV1_1Repository productRepository;

  @Nested
  @DisplayName("Create Product V1.1 - Functional Tests")
  class CreateProduct {

    @AdminTest
    void shouldCreateProductReturnCreated() throws Exception {
      ProductCreateDto productDto = createValidDto();

      mockMvc
          .perform(
              post(ApiPaths.PRODUCT_V1_1)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(DataGenUtils.toJson(productDto)))
          .andExpect(status().isCreated());

      ProductV1_1 saved = productRepository.findByBarcode(productDto.getBarcode()).orElse(null);
      assertThat(saved).isNotNull();
      assertThat(saved.getName()).isEqualTo(productDto.getName());
      assertThat(saved.getBarcode()).isEqualTo(productDto.getBarcode());
      assertThat(saved.getUnitType()).isEqualTo(UnitType.valueOf(productDto.getUnitType()));
    }

    @AdminTest
    void shouldCreateProductReturnConflict() throws Exception {
      ProductCreateDto productDto = createValidDto();
      String fixedBarcode = "1234567890123";
      productDto.setBarcode(fixedBarcode);

      productRepository.save(
          ProductV1_1.builder()
              .name(productDto.getName())
              .barcode(fixedBarcode)
              .stockQuantity(productDto.getStockQuantity())
              .minimumStock(productDto.getMinimumStock())
              .price(productDto.getPrice())
              .cost(productDto.getCost())
              .discount(productDto.getDiscount())
              .unitType(UnitType.valueOf(productDto.getUnitType()))
              .build());

      mockMvc
          .perform(
              post(ApiPaths.PRODUCT_V1_1)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(DataGenUtils.toJson(productDto)))
          .andExpect(status().isConflict());
    }

    @AdminTest
    void shouldCreateProductReturnBadRequestDueToValidation() throws Exception {
      ProductCreateDto invalidDto = ProductCreateDto.builder().build(); // All null

      mockMvc
          .perform(
              post(ApiPaths.PRODUCT_V1_1)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(DataGenUtils.toJson(invalidDto)))
          .andExpect(status().isBadRequest());
    }
  }

  @Nested
  @DisplayName("Create Product V1.1 - Security Tests")
  class SecurityTests {

    @InventoryManagerTest
    void shouldCreateProductSucceedWithInventoryManager() throws Exception {
      ProductCreateDto productDto = createValidDto();

      mockMvc
          .perform(
              post(ApiPaths.PRODUCT_V1_1)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(DataGenUtils.toJson(productDto)))
          .andExpect(status().isCreated());
    }

    @SalesAttendantTest
    void shouldCreateProductFailWithSalesAttendant() throws Exception {
      ProductCreateDto productDto = createValidDto();

      mockMvc
          .perform(
              post(ApiPaths.PRODUCT_V1_1)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(DataGenUtils.toJson(productDto)))
          .andExpect(status().isForbidden());
    }

    @Test
    void shouldCreateProductFailWhenUnauthenticated() throws Exception {
      ProductCreateDto productDto = createValidDto();

      mockMvc
          .perform(
              post(ApiPaths.PRODUCT_V1_1)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(DataGenUtils.toJson(productDto)))
          .andExpect(status().isUnauthorized());
    }
  }

  private ProductCreateDto createValidDto() {
    return ProductCreateDto.builder()
        .name(DataGenUtils.faker.commerce().productName())
        .barcode(DataGenUtils.faker.number().digits(13))
        .stockQuantity(new BigDecimal("10.000"))
        .minimumStock(new BigDecimal("2.000"))
        .price(new BigDecimal("100.00"))
        .cost(new BigDecimal("50.00"))
        .discount(new BigDecimal("5.00"))
        .unitType(UnitType.UNIT.name())
        .build();
  }
}
