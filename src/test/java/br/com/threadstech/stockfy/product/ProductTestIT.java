package br.com.threadstech.stockfy.product;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import br.com.threadstech.stockfy.AdminTest;
import br.com.threadstech.stockfy.IntegrationTests;
import br.com.threadstech.stockfy.api.ApiPaths;
import br.com.threadstech.stockfy.repository.ProductRepository;
import br.com.threadstech.stockfy.utils.DataGenUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.postgresql.PostgreSQLContainer;

@IntegrationTests
public class ProductTestIT {

  @Container
  static PostgreSQLContainer postgreSQLContainer = new PostgreSQLContainer("postgres:16-alpine");

  @Autowired private MockMvc mockMvc;
  @Autowired private ProductRepository productRepository;

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


  }
}
