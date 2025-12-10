package br.com.threadstech.stockfy.product;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import br.com.threadstech.stockfy.AdminTest;
import br.com.threadstech.stockfy.IntegrationTests;
import br.com.threadstech.stockfy.PostgreTestContainer;
import br.com.threadstech.stockfy.api.ApiPaths;
import br.com.threadstech.stockfy.entity.Product;
import br.com.threadstech.stockfy.repository.ProductRepository;
import br.com.threadstech.stockfy.utils.DataGenUtils;
import br.com.threadstech.stockfy.web.dto.ProductCreateDto;
import br.com.threadstech.stockfy.web.dto.ProductResponseDto;
import br.com.threadstech.stockfy.web.dto.ProductUpdateDto;
import br.com.threadstech.stockfy.web.dto.mapper.ProductMapper;
import com.jayway.jsonpath.JsonPath;
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
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@Slf4j
@IntegrationTests
@Import(PostgreTestContainer.class)
public class ProductTestIT {

  @Autowired private MockMvc mockMvc;
  @Autowired private ProductRepository productRepository;
  @Autowired private ProductMapper productMapper;

  @Nested
  @DisplayName("Create product")
  class CreateProduct {

    @AdminTest
    void shouldCreateProductWithReturnStatusCreated() throws Exception {
      mockMvc
          .perform(
              post(ApiPaths.PRODUCT)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(DataGenUtils.validProductCreateJson()))
          .andExpect(status().isCreated());
    }

    @AdminTest
    void shouldCreateProductWithReturnStatusConflict() throws Exception {
      String product = DataGenUtils.validProductCreateJson();
      mockMvc.perform(
          post(ApiPaths.PRODUCT).contentType(MediaType.APPLICATION_JSON).content(product));
      mockMvc
          .perform(post(ApiPaths.PRODUCT).contentType(MediaType.APPLICATION_JSON).content(product))
          .andExpect(status().isConflict());
    }

    @AdminTest
    void shouldCreateProductWithReturnStatusBadRequest() throws Exception {
      String nullFields = DataGenUtils.nullFieldsProductCreateJson();
      String invalidProduct = DataGenUtils.invalidSizeProductCreateJson();
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
                  .content(DataGenUtils.validProductCreateJson()))
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
      products.forEach(ProductTestIT.this::validateProductResponse);
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
      validateProductResponse(responseProduct);
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
      products.forEach(ProductTestIT.this::validateProductResponse);
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
      // FIXME: Corrigir problema 400 bad request
      String productNameUpdate = "NOME DE PRODUTO TESTE";
      Product product = saveProduct();
      String productUpdateJson = String.format("{ 'name': '%s' }", productNameUpdate);
      mockMvc
          .perform(
              patch(getByIdPath(product.getId()))
                  .content(productUpdateJson)
                  .contentType(MediaType.APPLICATION_JSON))
          .andExpect(status().isNoContent());
      Product updatedProduct = productRepository.findById(product.getId()).orElseGet(() -> null);
      assertThat(updatedProduct).isNotNull();
      assertThat(updatedProduct.getName()).isNotEqualTo(product.getName());
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

  private Product saveProduct(String name) {
    ProductCreateDto productDto = DataGenUtils.validProductCreateDto(name);
    Product productMapped = productMapper.toProduct(productDto);
    return productRepository.save(productMapped);
  }

  private Product saveProduct() {
    ProductCreateDto productDto = DataGenUtils.validProductCreateDto();
    Product productMapped = productMapper.toProduct(productDto);
    return productRepository.save(productMapped);
  }

  private void validateProductResponse(Map<String, Object> responseProduct) {
    Field[] dtoFields = ProductResponseDto.class.getDeclaredFields();
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
}
