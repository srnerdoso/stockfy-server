package br.com.threadstech.stockfy.modules.product;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.threadstech.stockfy.annotations.AdminTest;
import br.com.threadstech.stockfy.annotations.IntegrationTests;
import br.com.threadstech.stockfy.annotations.ProductClerkTest;
import br.com.threadstech.stockfy.exception.ProductUniqueViolationException;
import br.com.threadstech.stockfy.modules.product.dto.ProductCreateDto;
import br.com.threadstech.stockfy.modules.product.dto.ProductResponseDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@IntegrationTests
class ProductControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockBean private ProductService productService;

  private static final String PRODUCT_V1_1 = "/api/v1_1/products";

  @AdminTest
  @DisplayName("shouldCreateProductReturnCreatedWhenUserIsAdmin")
  void shouldCreateProductReturnCreatedWhenUserIsAdmin() throws Exception {
    when(productService.createProduct(any(ProductCreateDto.class)))
        .thenReturn(ProductResponseDto.builder().build());

    mockMvc
        .perform(
            post(PRODUCT_V1_1)
                .contentType(MediaType.APPLICATION_JSON)
                .content(ProductTestData.VALID_CREATE_JSON))
        .andExpect(status().isCreated());
  }

  @ProductClerkTest
  @DisplayName("shouldCreateProductReturnCreatedWhenUserIsProductClerk")
  void shouldCreateProductReturnCreatedWhenUserIsProductClerk() throws Exception {
    when(productService.createProduct(any(ProductCreateDto.class)))
        .thenReturn(ProductResponseDto.builder().build());

    mockMvc
        .perform(
            post(PRODUCT_V1_1)
                .contentType(MediaType.APPLICATION_JSON)
                .content(ProductTestData.VALID_CREATE_JSON))
        .andExpect(status().isCreated());
  }

  @Test
  @WithMockUser(roles = "SALES_ATTENDANT")
  @DisplayName("shouldReturnForbiddenWhenUserIsSalesAttendant")
  void shouldReturnForbiddenWhenUserIsSalesAttendant() throws Exception {
    mockMvc
        .perform(
            post(PRODUCT_V1_1)
                .contentType(MediaType.APPLICATION_JSON)
                .content(ProductTestData.VALID_CREATE_JSON))
        .andExpect(status().isForbidden());
  }

  @AdminTest
  @DisplayName("shouldReturnBadRequestWhenDataIsInvalid")
  void shouldReturnBadRequestWhenDataIsInvalid() throws Exception {
    mockMvc
        .perform(
            post(PRODUCT_V1_1)
                .contentType(MediaType.APPLICATION_JSON)
                .content(ProductTestData.INVALID_JSON_MISSING_REQUIRED))
        .andExpect(status().isBadRequest());
  }

  @AdminTest
  @DisplayName("shouldReturnConflictWhenBarcodeExists")
  void shouldReturnConflictWhenBarcodeExists() throws Exception {
    when(productService.createProduct(any(ProductCreateDto.class)))
        .thenThrow(new ProductUniqueViolationException("barcode"));

    mockMvc
        .perform(
            post(PRODUCT_V1_1)
                .contentType(MediaType.APPLICATION_JSON)
                .content(ProductTestData.VALID_CREATE_JSON))
        .andExpect(status().isConflict());
  }
}
