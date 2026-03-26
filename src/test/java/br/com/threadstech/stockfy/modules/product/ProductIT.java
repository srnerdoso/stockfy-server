package br.com.threadstech.stockfy.modules.product;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.threadstech.stockfy.annotations.AdminTest;
import br.com.threadstech.stockfy.annotations.IntegrationTests;
import br.com.threadstech.stockfy.annotations.InventoryManagerTest;
import br.com.threadstech.stockfy.annotations.ProductClerkTest;
import br.com.threadstech.stockfy.annotations.SalesAttendantTest;
import br.com.threadstech.stockfy.api.ApiPaths;
import br.com.threadstech.stockfy.core.enums.UnitType;
import br.com.threadstech.stockfy.modules.product.dto.ProductCreateDto;
import br.com.threadstech.stockfy.utils.DataGenUtils;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@IntegrationTests
public class ProductIT {

  @Autowired private MockMvc mockMvc;
  @Autowired private ProductV1_1Repository productRepository;

  @BeforeEach
  void setUp() {
    productRepository.deleteAll();
  }

  @Nested
  @DisplayName("Create Product V1.1")
  class CreateProduct {

    @AdminTest
    void shouldCreateProductSucceedWithAdmin() throws Exception {
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
    }

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

    @ProductClerkTest
    void shouldCreateProductFailWithProductClerk() throws Exception {
      ProductCreateDto productDto = createValidDto();

      mockMvc
          .perform(
              post(ApiPaths.PRODUCT_V1_1)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(DataGenUtils.toJson(productDto)))
          .andExpect(status().isForbidden());
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

  @Nested
  @DisplayName("Find Product By Id V1.1")
  class FindProductById {

    @AdminTest
    void shouldFindProductByIdSucceedWithAdmin() throws Exception {
      ProductV1_1 product = saveProductEntity();

      String response =
          mockMvc
              .perform(get(ApiPaths.PRODUCT_V1_1 + "/" + product.getId() + "/id"))
              .andExpect(status().isOk())
              .andExpect(jsonPath("$.id").doesNotExist())
              .andReturn()
              .getResponse()
              .getContentAsString();

      assertThat(com.jayway.jsonpath.JsonPath.<String>read(response, "$.name"))
          .isEqualTo(product.getName());
      assertThat(com.jayway.jsonpath.JsonPath.<String>read(response, "$.barcode"))
          .isEqualTo(product.getBarcode());
      assertThat(
              new BigDecimal(
                  com.jayway.jsonpath.JsonPath.read(response, "$.stockQuantity").toString()))
          .isEqualByComparingTo(product.getStockQuantity());
      assertThat(
              new BigDecimal(
                  com.jayway.jsonpath.JsonPath.read(response, "$.minimumStock").toString()))
          .isEqualByComparingTo(product.getMinimumStock());
      assertThat(new BigDecimal(com.jayway.jsonpath.JsonPath.read(response, "$.price").toString()))
          .isEqualByComparingTo(product.getPrice());
      assertThat(new BigDecimal(com.jayway.jsonpath.JsonPath.read(response, "$.cost").toString()))
          .isEqualByComparingTo(product.getCost());
      assertThat(
              new BigDecimal(com.jayway.jsonpath.JsonPath.read(response, "$.discount").toString()))
          .isEqualByComparingTo(product.getDiscount());
      assertThat(com.jayway.jsonpath.JsonPath.<String>read(response, "$.unitType"))
          .isEqualTo(product.getUnitType().name());
    }

    @InventoryManagerTest
    void shouldFindProductByIdSucceedWithInventoryManager() throws Exception {
      ProductV1_1 product = saveProductEntity();

      mockMvc
          .perform(get(ApiPaths.PRODUCT_V1_1 + "/" + product.getId() + "/id"))
          .andExpect(status().isOk());
    }

    @SalesAttendantTest
    void shouldFindProductByIdSucceedWithSalesAttendant() throws Exception {
      ProductV1_1 product = saveProductEntity();

      mockMvc
          .perform(get(ApiPaths.PRODUCT_V1_1 + "/" + product.getId() + "/id"))
          .andExpect(status().isOk());
    }

    @ProductClerkTest
    void shouldFindProductByIdFailWithProductClerk() throws Exception {
      ProductV1_1 product = saveProductEntity();

      mockMvc
          .perform(get(ApiPaths.PRODUCT_V1_1 + "/" + product.getId() + "/id"))
          .andExpect(status().isForbidden());
    }

    @AdminTest
    void shouldReturn404WhenProductNotFound() throws Exception {
      mockMvc
          .perform(get(ApiPaths.PRODUCT_V1_1 + "/99999/id"))
          .andExpect(status().isNotFound())
          .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("99999")));
    }

    @Test
    void shouldReturn401WhenUnauthenticated() throws Exception {
      mockMvc.perform(get(ApiPaths.PRODUCT_V1_1 + "/1/id")).andExpect(status().isUnauthorized());
    }
  }

  // FIXME: Alguns métodos não estão verificando se as mensagens de erro retornadas estão corretas
  @Nested
  @DisplayName("Product Validation Tests")
  class ValidationTests {

    @Test
    @AdminTest
    @DisplayName(
        "Should validate all annotations in ProductCreateDto and ensure user-friendly messages")
    void shouldValidateAllAnnotationsAndEnsureUserFriendlyMessages() throws Exception {
      ProductCreateDto invalidDto = new ProductCreateDto();

      MvcResult result =
          mockMvc
              .perform(
                  post(ApiPaths.PRODUCT_V1_1)
                      .contentType(MediaType.APPLICATION_JSON)
                      .content(DataGenUtils.toJson(invalidDto)))
              .andExpect(status().isBadRequest())
              .andExpect(
                  jsonPath("$.message")
                      .value(
                          "Validation error in the provided data. Please check the fields and try again."))
              .andReturn();

      String content = result.getResponse().getContentAsString();
      Map<String, Object> response = DataGenUtils.objectMapper.readValue(content, Map.class);
      Map<String, String> errors = (Map<String, String>) response.get("errors");

      assertThat(errors).isNotNull();

      for (Field field : ProductCreateDto.class.getDeclaredFields()) {
        if (field.isSynthetic()) continue;

        String fieldName = field.getName();
        if (errors.containsKey(fieldName)) {
          String errorMessage = errors.get(fieldName);

          assertThat(errorMessage)
              .as("Error message for field '%s' should be resolved", fieldName)
              .doesNotContain("{")
              .doesNotContain("}");
        }
      }
    }

    @Test
    @AdminTest
    @DisplayName(
        "Should validate specific invalid values for @Size, @Positive, @Barcode, @UnitType")
    void shouldValidateSpecificInvalidValues() throws Exception {
      ProductCreateDto dto =
          ProductCreateDto.builder()
              .name("") // NotBlank, Size
              .barcode("invalid") // Barcode
              .stockQuantity(new BigDecimal("-10")) // Positive
              .minimumStock(new BigDecimal("-1")) // PositiveOrZero
              .price(new BigDecimal("0")) // Positive
              .cost(new BigDecimal("-5")) // Positive
              .discount(new BigDecimal("-2")) // PositiveOrZero
              .unitType("INVALID_TYPE") // UnitType
              .build();

      mockMvc
          .perform(
              post(ApiPaths.PRODUCT_V1_1)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(DataGenUtils.toJson(dto)))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.errors.name").exists())
          .andExpect(jsonPath("$.errors.barcode").exists())
          .andExpect(jsonPath("$.errors.stockQuantity").exists())
          .andExpect(jsonPath("$.errors.minimumStock").exists())
          .andExpect(jsonPath("$.errors.price").exists())
          .andExpect(jsonPath("$.errors.cost").exists())
          .andExpect(jsonPath("$.errors.discount").exists())
          .andExpect(jsonPath("$.errors.unitType").exists())
          .andExpect(
              jsonPath("$.errors.*")
                  .value(
                      org.hamcrest.Matchers.everyItem(
                          org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("{")))))
          .andExpect(
              jsonPath("$.errors.*")
                  .value(
                      org.hamcrest.Matchers.everyItem(
                          org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("}")))));
    }

    @Test
    @AdminTest
    @DisplayName("Should validate null values for required fields")
    void shouldValidateNullValues() throws Exception {
      ProductCreateDto dto =
          ProductCreateDto.builder()
              .name(null)
              .barcode(null)
              .stockQuantity(null)
              .minimumStock(null)
              .price(null)
              .cost(null)
              .unitType(null)
              .build();

      mockMvc
          .perform(
              post(ApiPaths.PRODUCT_V1_1)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(DataGenUtils.toJson(dto)))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.errors.name").exists())
          .andExpect(jsonPath("$.errors.barcode").exists())
          .andExpect(jsonPath("$.errors.stockQuantity").exists())
          .andExpect(jsonPath("$.errors.minimumStock").exists())
          .andExpect(jsonPath("$.errors.price").exists())
          .andExpect(jsonPath("$.errors.cost").exists())
          .andExpect(jsonPath("$.errors.unitType").exists());
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

  private ProductV1_1 saveProductEntity() {
    return productRepository.save(
        ProductV1_1.builder()
            .name(DataGenUtils.faker.commerce().productName())
            .barcode(DataGenUtils.faker.number().digits(13))
            .stockQuantity(new BigDecimal("10.000"))
            .minimumStock(new BigDecimal("2.000"))
            .price(new BigDecimal("100.00"))
            .cost(new BigDecimal("50.00"))
            .discount(new BigDecimal("5.00"))
            .unitType(UnitType.UNIT)
            .build());
  }
}
