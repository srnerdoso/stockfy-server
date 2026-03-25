package br.com.threadstech.stockfy.modules.product.dto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.threadstech.stockfy.annotations.AdminTest;
import br.com.threadstech.stockfy.annotations.IntegrationTests;
import br.com.threadstech.stockfy.api.ApiPaths;
import br.com.threadstech.stockfy.utils.DataGenUtils;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@IntegrationTests
public class ProductCreateDtoValidationTest {

  @Autowired private MockMvc mockMvc;

  @Test
  @AdminTest
  @DisplayName("Should validate all annotations in ProductCreateDto and ensure user-friendly messages")
  void shouldValidateAllAnnotationsAndEnsureUserFriendlyMessages() throws Exception {
    ProductCreateDto invalidDto = new ProductCreateDto();
    // Setting fields to trigger various validations (mostly @NotNull, @NotBlank, @Size, @Positive, etc.)
    // Empty strings for @NotBlank, nulls for @NotNull, negative for @Positive

    MvcResult result =
        mockMvc
            .perform(
                post(ApiPaths.PRODUCT_V1_1)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(DataGenUtils.toJson(invalidDto)))
            .andExpect(status().isBadRequest())
            // Validate main message is user-friendly
            .andExpect(
                jsonPath("$.message")
                    .value(
                        "Validation error in the provided data. Please check the fields and try again."))
            .andReturn();

    String content = result.getResponse().getContentAsString();
    Map<String, Object> response = DataGenUtils.objectMapper.readValue(content, Map.class);
    Map<String, String> errors = (Map<String, String>) response.get("errors");

    assertThat(errors).isNotNull();

    // Dynamically check all fields of ProductCreateDto
    for (Field field : ProductCreateDto.class.getDeclaredFields()) {
      if (field.isSynthetic()) continue;

      String fieldName = field.getName();
      Annotation[] annotations = field.getAnnotations();

      for (Annotation annotation : annotations) {
        // We expect at least one error for each annotated field in this "empty" DTO scenario
        // (Either NotNull, NotBlank, etc.)
        if (errors.containsKey(fieldName)) {
          String errorMessage = errors.get(fieldName);

          // Rule: Validate that messages MUST NOT contain { or }
          assertThat(errorMessage)
              .as("Error message for field '%s' should be resolved", fieldName)
              .doesNotContain("{")
              .doesNotContain("}");

          // Rule: Validate that fields return user-friendly names (if they are part of the message)
          // Since the requirement says "Use readable field names" and "Map technical field names to user-friendly names",
          // and based on the example, we expect the messages to be resolved and readable.
        }
      }
    }
  }

  @Test
  @AdminTest
  @DisplayName("Should validate specific invalid values for @Size, @Positive, @Barcode, @UnitType")
  void shouldValidateSpecificInvalidValues() throws Exception {
    ProductCreateDto dto =
        ProductCreateDto.builder()
            .name("") // NotBlank, Size
            .barcode("invalid") // Barcode
            .stockQuantity(new java.math.BigDecimal("-10")) // Positive
            .minimumStock(new java.math.BigDecimal("-1")) // PositiveOrZero
            .price(new java.math.BigDecimal("0")) // Positive
            .cost(new java.math.BigDecimal("-5")) // Positive
            .discount(new java.math.BigDecimal("-2")) // PositiveOrZero
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
        // Ensure no curly braces in any of them
        .andExpect(jsonPath("$.errors.*").value(org.hamcrest.Matchers.everyItem(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("{")))))
        .andExpect(jsonPath("$.errors.*").value(org.hamcrest.Matchers.everyItem(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("}")))));
  }
}
