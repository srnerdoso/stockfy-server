package br.com.threadstech.stockfy.product;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;

import br.com.threadstech.stockfy.AdminTest;
import br.com.threadstech.stockfy.IntegrationTests;
import br.com.threadstech.stockfy.PostgreTestContainer;
import br.com.threadstech.stockfy.api.ApiPaths;
import br.com.threadstech.stockfy.entity.Product;
import br.com.threadstech.stockfy.repository.ProductRepository;
import br.com.threadstech.stockfy.utils.DataGenUtils;
import br.com.threadstech.stockfy.web.dto.ProductCreateDto;
import br.com.threadstech.stockfy.web.dto.mapper.ProductMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

import java.util.ArrayList;
import java.util.List;

@IntegrationTests
@Import(PostgreTestContainer.class)
public class ProductTestIT {

  // TODO: Trocar mockMvc por mockMvcTester
  // TODO: Implementar testes com verificação de corpo de resposta

  @Autowired private MockMvc mockMvc;
  @Autowired private MockMvcTester mockMvcTester;
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

    @BeforeEach
    void setUp() {
      if (productRepository.count() == 0) {
        List<Product> products = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
          products.add(saveProduct());
        }
        productRepository.saveAll(products);
      }
    }

    @AdminTest
    void shouldFindAllProductsWithReturnStatusOk() throws Exception {
      mockMvc.perform(get(ApiPaths.PRODUCT)).andDo(print()).andExpect(status().isOk());
    }

    @AdminTest
    void shouldFindProductByBarCodeWithReturnStatusOk() throws Exception {
      Product product = saveProduct();
      mockMvc
          .perform(get(getPatternPath(product.getBarCode(), "barcode")))
          .andDo(print())
          .andExpect(status().isOk());
    }

    @AdminTest
    void shouldFindAllProductsByNameWithReturnStatusOk() throws Exception {
      Product product = saveProduct();
      mockMvc
          .perform(get(getPatternPath(product.getName(), "name")))
          .andDo(print())
          .andExpect(status().isOk());
    }

    @Test
    void shouldFindProductWithNotAuthenticatedUserWithReturnStatusUnauthorized() throws Exception {
      Product product = saveProduct();
      mockMvc.perform(get(ApiPaths.PRODUCT)).andExpect(status().isUnauthorized());
      mockMvc
          .perform(get(getPatternPath(product.getBarCode(), "barcode")))
          .andDo(print())
          .andExpect(status().isUnauthorized());
      mockMvc
          .perform(get(getPatternPath(product.getName(), "name")))
          .andDo(print())
          .andExpect(status().isUnauthorized());
    }

    @AdminTest
    void shouldFindProductByBarCodeWithReturnStatusNotFound() throws Exception {
      productRepository.deleteAll();
      var response =
          mockMvcTester
              .get()
              .uri(getPatternPath("123456789", "barcode"))
              .exchange();
    }
  }

  private String getPatternPath(String pathVar, String resource) {
    return ApiPaths.PRODUCT + "/" + pathVar + "/" + resource;
  }

  private Product saveProduct() {
    ProductCreateDto productDto = DataGenUtils.validProductCreateDto();
    Product productMapped = productMapper.toProduct(productDto);
    return productRepository.save(productMapped);
  }
}
