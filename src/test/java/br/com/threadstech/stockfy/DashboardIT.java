package br.com.threadstech.stockfy;

import br.com.threadstech.stockfy.annotations.AdminTest;
import br.com.threadstech.stockfy.annotations.IntegrationTests;
import br.com.threadstech.stockfy.annotations.InventoryManagerTest;
import br.com.threadstech.stockfy.annotations.SalesAttendantTest;
import br.com.threadstech.stockfy.api.ApiPaths;
import br.com.threadstech.stockfy.enums.MetricI18nKeys;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@IntegrationTests
@DisplayName("Dashboard Integration Tests")
@Sql(
    scripts = {
        "/sql/customer-contacts-insert.sql",
        "/sql/customer-addresses-insert.sql",
        "/sql/customers-insert.sql",
        "/sql/products-insert.sql",
        "/sql/payments-insert.sql"
    },
    executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/sql/payment-cleanup.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public class DashboardIT {

  @Autowired private MockMvc mockMvc;

  @Nested
  @DisplayName("Metrics Endpoint Tests")
  class MetricsTests {

    @AdminTest
    @DisplayName("Should return dashboard metrics")
    void shouldReturnMetrics() throws Exception {
      mockMvc
          .perform(get(ApiPaths.DASHBOARD + "/metrics"))
          .andExpect(status().isOk())
          .andExpect(content().contentType(MediaType.APPLICATION_JSON))
          .andExpect(jsonPath("$").isArray())
          .andExpect(jsonPath("$", hasSize(3)))
          .andExpect(jsonPath("$[0].i18nKey").value(MetricI18nKeys.TOTAL_SALES.name()))
          .andExpect(jsonPath("$[1].i18nKey").value(MetricI18nKeys.TOTAL_PROFIT.name()))
          .andExpect(jsonPath("$[2].i18nKey").value(MetricI18nKeys.TOTAL_CUSTOMERS.name()));
    }

    @InventoryManagerTest
    @DisplayName("Should return 403 Forbidden for inventory manager")
    void shouldReturnForbiddenForInventoryManager() throws Exception {
      mockMvc
          .perform(get(ApiPaths.DASHBOARD + "/metrics"))
          .andExpect(status().isForbidden());
    }

    @SalesAttendantTest
    @DisplayName("Should return 403 Forbidden for sales attendant")
    void shouldReturnForbiddenForSalesAttendant() throws Exception {
      mockMvc
          .perform(get(ApiPaths.DASHBOARD + "/metrics"))
          .andExpect(status().isForbidden());
    }
  }

  @AdminTest
  @DisplayName("Should return top products")
  void shouldReturnTopProducts() throws Exception {
    mockMvc
        .perform(get(ApiPaths.DASHBOARD + "/top-products").param("type", "UNIT"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$").isArray());
  }

  @AdminTest
  @DisplayName("Should return last sales")
  void shouldReturnLastSales() throws Exception {
    mockMvc
        .perform(get(ApiPaths.DASHBOARD + "/last-sales"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$").isArray());
  }

  @AdminTest
  @DisplayName("Should return audit events")
  void shouldReturnAuditEvents() throws Exception {
    mockMvc
        .perform(get(ApiPaths.DASHBOARD + "/audit"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$").isArray());
  }

  @AdminTest
  @DisplayName("Should return alerts")
  void shouldReturnAlerts() throws Exception {
    mockMvc
        .perform(get(ApiPaths.DASHBOARD + "/alerts"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$").isArray());
  }

  @AdminTest
  @DisplayName("Should return sales graph")
  void shouldReturnSalesGraph() throws Exception {
    mockMvc
        .perform(get(ApiPaths.DASHBOARD + "/sales").param("filter", "days"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$").isArray());
  }
}
