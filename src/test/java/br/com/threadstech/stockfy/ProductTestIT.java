package br.com.threadstech.stockfy;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import br.com.threadstech.stockfy.annotations.AdminTest;
import br.com.threadstech.stockfy.annotations.IntegrationTests;
import br.com.threadstech.stockfy.api.ApiPaths;
import br.com.threadstech.stockfy.entity.Product;
import br.com.threadstech.stockfy.repository.ProductRepository;
import br.com.threadstech.stockfy.utils.DataGenUtils;
import br.com.threadstech.stockfy.utils.ProductTestsUtils;
import br.com.threadstech.stockfy.web.dto.ProductAutocompleteResponseDto;
import br.com.threadstech.stockfy.web.dto.ProductCreateDto;
import br.com.threadstech.stockfy.web.dto.ProductSummaryResponseDto;
import br.com.threadstech.stockfy.web.dto.ProductUpdateDto;
import br.com.threadstech.stockfy.web.dto.mapper.ProductMapper;
import com.jayway.jsonpath.JsonPath;
import jakarta.persistence.EntityManager;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@Slf4j
@IntegrationTests
public class ProductTestIT {

  @Autowired private MockMvc mockMvc;
  @Autowired private ProductRepository productRepository;
  @Autowired private ProductMapper productMapper;
  @Autowired private EntityManager entityManager;

  @Nested
  @DisplayName("Create product")
  class CreateProduct {

    @AdminTest
    void shouldCreateProductWithReturnStatusCreated() throws Exception {
      ProductCreateDto productDto = ProductTestsUtils.validProductCreateDto();
      mockMvc
          .perform(
              post(ApiPaths.PRODUCT)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(DataGenUtils.toJson(productDto)))
          .andExpect(status().isCreated());

      Product productSaved =
          productRepository.findByBarCode(productDto.getBarCode()).orElseGet(() -> null);
      assertThat(productSaved).isNotNull();
      assertThat(productSaved.getBarCode()).isEqualTo(productDto.getBarCode());
      assertThat(productSaved.getName()).isEqualTo(productDto.getName());
      assertThat(productSaved.getStock()).isEqualTo(productDto.getStock());
      assertThat(productSaved.getPrice()).isEqualTo(productDto.getPrice());
      assertThat(productSaved.getCost()).isEqualTo(productDto.getCost());
      assertThat(productSaved.getProfit()).isEqualTo(productDto.getProfit());
      assertThat(productSaved.getDiscount()).isEqualTo(productDto.getDiscount());
      assertThat(productSaved.getDiscountPercentage())
          .isEqualTo(productDto.getDiscountPercentage());
      assertThat(productSaved.getType().name()).isEqualTo(productDto.getType());
    }

    @AdminTest
    void shouldCreateProductWithReturnStatusConflict() throws Exception {
      String product = ProductTestsUtils.validProductCreateJson();
      mockMvc.perform(
          post(ApiPaths.PRODUCT).contentType(MediaType.APPLICATION_JSON).content(product));
      mockMvc
          .perform(post(ApiPaths.PRODUCT).contentType(MediaType.APPLICATION_JSON).content(product))
          .andExpect(status().isConflict());
    }

    @AdminTest
    void shouldCreateProductWithReturnStatusBadRequest() throws Exception {
      String nullFields = ProductTestsUtils.nullFieldsProductCreateJson();
      String invalidProduct = ProductTestsUtils.invalidSizeProductCreateJson();
      mockMvc
          .perform(
              post(ApiPaths.PRODUCT).contentType(MediaType.APPLICATION_JSON).content(nullFields))
          .andExpect(status().isBadRequest());
      mockMvc
          .perform(
              post(ApiPaths.PRODUCT)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(invalidProduct))
          .andExpect(status().isBadRequest());
    }

    @Test
    void shouldCreateProductWithReturnStatusUnauthorized() throws Exception {
      mockMvc
          .perform(
              post(ApiPaths.PRODUCT)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(ProductTestsUtils.validProductCreateJson()))
          .andExpect(status().isUnauthorized());
    }
  }

  @Nested
  @DisplayName("Find product/products")
  class FindProduct {

    int size = 2;

    @BeforeEach
    void setUp() {
      productRepository.deleteAll();
      List<Product> products = new ArrayList<>();
      for (int i = 0; i < size; i++) {
        products.add(saveProduct());
      }
      productRepository.saveAll(products);
    }

    @AdminTest
    void shouldFindAllProductsWithReturnStatusOk() throws Exception {
      String response =
          mockMvc
              .perform(get(ApiPaths.PRODUCT))
              .andDo(print())
              .andExpect(status().isOk())
              .andExpect(content().contentType(MediaType.APPLICATION_JSON))
              .andExpect(jsonPath("$.content").isArray())
              .andExpect(jsonPath("$.content.length()").value(size))
              .andReturn()
              .getResponse()
              .getContentAsString();

      List<Map<String, Object>> products = JsonPath.read(response, "$.content");
      products.forEach(ProductTestIT.this::validateProductSummaryResponse);
    }

    @AdminTest
    void shouldFindProductByBarCodeWithReturnStatusOk() throws Exception {
      Product product = saveProduct();
      String responseBody =
          mockMvc
              .perform(get(getByBarCodePath(product.getBarCode())))
              .andDo(print())
              .andExpect(status().isOk())
              .andExpect(content().contentType(MediaType.APPLICATION_JSON))
              .andReturn()
              .getResponse()
              .getContentAsString();

      Map<String, Object> responseProduct = JsonPath.read(responseBody, "$");
      validateProductSummaryResponse(responseProduct);
    }

    @AdminTest
    void shouldFindAllProductsByNameWithReturnStatusOk() throws Exception {
      String productName = "Laranja";
      for (int i = 0; i < size; i++) {
        saveProduct(productName);
      }
      String response =
          mockMvc
              .perform(get(getByNamePath(productName)))
              .andDo(print())
              .andExpect(status().isOk())
              .andExpect(content().contentType(MediaType.APPLICATION_JSON))
              .andExpect(jsonPath("$.content").isArray())
              .andExpect(jsonPath("$.content.length()").value(size))
              .andReturn()
              .getResponse()
              .getContentAsString();

      List<Map<String, Object>> products = JsonPath.read(response, "$.content");
      products.forEach(ProductTestIT.this::validateProductSummaryResponse);
    }

    @AdminTest
    void shouldFindAutocompleteWithReturnStatusOk() throws Exception {
      String productName = "Laranja";
      for (int i = 0; i < size; i++) {
        saveProduct(productName);
      }
      String response =
          mockMvc
              .perform(get(ApiPaths.PRODUCT + "/autocomplete?name=" + productName))
              .andDo(print())
              .andExpect(status().isOk())
              .andExpect(content().contentType(MediaType.APPLICATION_JSON))
              .andExpect(jsonPath("$.content").isArray())
              .andExpect(jsonPath("$.content.length()").value(size))
              .andReturn()
              .getResponse()
              .getContentAsString();

      List<Map<String, Object>> products = JsonPath.read(response, "$.content");
      products.forEach(ProductTestIT.this::validateAutocompleteResponse);
    }

    @Test
    void shouldFindProductWithNotAuthenticatedUserWithReturnStatusUnauthorized() throws Exception {
      Product product = saveProduct();
      mockMvc.perform(get(ApiPaths.PRODUCT)).andExpect(status().isUnauthorized());
      mockMvc
          .perform(get(getByBarCodePath(product.getBarCode())))
          .andDo(print())
          .andExpect(status().isUnauthorized());
      mockMvc
          .perform(get(getByNamePath(product.getName())))
          .andDo(print())
          .andExpect(status().isUnauthorized());
    }

    @AdminTest
    void shouldFindProductByBarCodeWithReturnStatusNotFound() throws Exception {
      productRepository.deleteAll();
      mockMvc
          .perform(get(getByBarCodePath("123456789")))
          .andDo(print())
          .andExpect(status().isNotFound());
    }
  }

  @Nested
  @DisplayName("Delete product")
  class DeleteProduct {

    @AdminTest
    void shouldDeleteProductWithReturnStatusNoContent() throws Exception {
      Product product = saveProduct();
      mockMvc.perform(delete(getByIdPath(product.getId()))).andExpect(status().isNoContent());

      Product productDeleted =
          (Product)
              entityManager
                  .createNativeQuery("SELECT * FROM products where id = :id", Product.class)
                  .setParameter("id", product.getId())
                  .getSingleResult();
      assertThat(productDeleted).isNotNull();
      assertThat(productDeleted.getBarCode())
          .isEqualTo(product.getBarCode() + "_deleted_" + product.getId());
      assertThat(productDeleted.isDeleted()).isEqualTo(true);
    }

    @Test
    void shouldDeleteProductWithReturnStatusUnauthorized() throws Exception {
      Product product = saveProduct();
      mockMvc.perform(delete(getByIdPath(product.getId()))).andExpect(status().isUnauthorized());
    }
  }

  @Nested
  @DisplayName("Update product")
  class UpdateProduct {

    @AdminTest
    void shouldUpdateProductWithReturnStatusNoContent() throws Exception {
      Product product = saveProduct();
      ProductUpdateDto productUpdateDto = ProductTestsUtils.validProductUpdateDto();

      String productUpdateJson = DataGenUtils.toJson(productUpdateDto);
      mockMvc
          .perform(
              patch(getByIdPath(product.getId()))
                  .content(productUpdateJson)
                  .contentType(MediaType.APPLICATION_JSON))
          .andExpect(status().isNoContent());
      Product updatedProduct = productRepository.findById(product.getId()).orElseGet(() -> null);

      assertThat(updatedProduct).isNotNull();
      assertThat(updatedProduct.getBarCode()).isEqualTo(productUpdateDto.getBarCode());
      assertThat(updatedProduct.getName()).isEqualTo(productUpdateDto.getName());
      assertThat(updatedProduct.getStock()).isEqualTo(productUpdateDto.getStock());
      assertThat(updatedProduct.getPrice()).isEqualTo(productUpdateDto.getPrice());
      assertThat(updatedProduct.getCost()).isEqualTo(productUpdateDto.getCost());
      assertThat(updatedProduct.getProfit()).isEqualTo(productUpdateDto.getProfit());
      assertThat(updatedProduct.getDiscount()).isEqualTo(productUpdateDto.getDiscount());
      assertThat(updatedProduct.getDiscountPercentage())
          .isEqualTo(productUpdateDto.getDiscountPercentage());
      assertThat(updatedProduct.getType().name()).isEqualTo(productUpdateDto.getType());
    }

    @Test
    void shouldUpdateProductWithReturnStatusUnauthorized() throws Exception {
      Product product = saveProduct();
      mockMvc
          .perform(
              patch(getByIdPath(product.getId()))
                  .content(ProductTestsUtils.validProductUpdateJson())
                  .contentType(MediaType.APPLICATION_JSON))
          .andExpect(status().isUnauthorized());
    }

    @AdminTest
    void shouldUpdateProductWithReturnStatusNotFound() throws Exception {
      mockMvc
          .perform(
              patch(getByIdPath(151256191561986L))
                  .content(ProductTestsUtils.validProductUpdateJson())
                  .contentType(MediaType.APPLICATION_JSON))
          .andExpect(status().isNotFound());
    }

    @AdminTest
    void shouldUpdateProductWithReturnStatusConflict() throws Exception {
      Product product = saveProduct();
      Product product2 = saveProduct();

      ProductUpdateDto productUpdateDto = ProductTestsUtils.validProductUpdateDto();
      productUpdateDto.setBarCode(product.getBarCode());
      String productUpdateJson = DataGenUtils.toJson(productUpdateDto);

      mockMvc
          .perform(
              patch(getByIdPath(product2.getId()))
                  .content(productUpdateJson)
                  .contentType(MediaType.APPLICATION_JSON))
          .andDo(print())
          .andExpect(status().isConflict());
    }
  }

  private String getPatternPathResource(String pathVar, String resource) {
    return ApiPaths.PRODUCT + "/" + pathVar + "/" + resource;
  }

  private String getByBarCodePath(String barCode) {
    return getPatternPathResource(barCode, "barcode");
  }

  private String getByNamePath(String name) {
    return getPatternPathResource(name, "name");
  }

  private String getByIdPath(Long id) {
    return getPatternPathResource(id.toString(), "id");
  }

  private void saveProduct(String name) {
    ProductCreateDto productDto = ProductTestsUtils.validProductCreateDto(name);
    Product productMapped = productMapper.toProduct(productDto);
    productRepository.save(productMapped);
  }

  private Product saveProduct() {
    ProductCreateDto productDto = ProductTestsUtils.validProductCreateDto();
    Product productMapped = productMapper.toProduct(productDto);
    return productRepository.saveAndFlush(productMapped);
  }

  private void validateProductSummaryResponse(Map<String, Object> responseProduct) {
    Field[] dtoFields = ProductSummaryResponseDto.class.getDeclaredFields();
    List<String> expectedFieldNames = Arrays.stream(dtoFields).map(Field::getName).toList();
    log.info("Expected field names: {}", expectedFieldNames);

    expectedFieldNames.forEach(
        fieldName -> {
          log.info(
              "Product '{}' contains field '{}': {}",
              responseProduct.get("name"),
              fieldName,
              responseProduct.containsKey(fieldName));
          assertThat(responseProduct)
              .as("Product should have field '%s' defined in response.", fieldName)
              .containsKey(fieldName);
        });
  }

  private void validateAutocompleteResponse(Map<String, Object> responseProduct) {
    Field[] dtoFields = ProductAutocompleteResponseDto.class.getDeclaredFields();
    List<String> expectedFieldNames = Arrays.stream(dtoFields).map(Field::getName).toList();
    log.info("Expected field names from autocomplete: {}", expectedFieldNames);

    expectedFieldNames.forEach(
        fieldName -> {
          log.info(
              "Product Autocomplete '{}' contains field '{}': {}",
              responseProduct.get("name"),
              fieldName,
              responseProduct.containsKey(fieldName));
          assertThat(responseProduct)
              .as("Product should have field '%s' defined in response.", fieldName)
              .containsKey(fieldName);
        });
  }
}
