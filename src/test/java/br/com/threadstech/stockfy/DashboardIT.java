package br.com.threadstech.stockfy;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import br.com.threadstech.stockfy.annotations.AdminTest;
import br.com.threadstech.stockfy.annotations.IntegrationTests;
import br.com.threadstech.stockfy.api.ApiPaths;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@IntegrationTests
@DisplayName("Dashboard Integration Tests")
public class DashboardIT {

  @Autowired private MockMvc mockMvc;

  @Nested
  @DisplayName("Metrics Endpoints")
  class MetricsEndpoints {

    @AdminTest
    @DisplayName("Should return dashboard metrics")
    void shouldReturnMetrics() throws Exception {
      mockMvc
          .perform(get(ApiPaths.DASHBOARD + "/metrics"))
          .andExpect(status().isOk())
          .andExpect(content().contentType(MediaType.APPLICATION_JSON))
          .andExpect(jsonPath("$").isArray());
    }

    @AdminTest
    @DisplayName("Should return top products")
    void shouldReturnTopProducts() throws Exception {
      mockMvc
          .perform(get(ApiPaths.DASHBOARD + "/top-products"))
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
          .perform(get(ApiPaths.DASHBOARD + "/audit-events"))
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
          .perform(get(ApiPaths.DASHBOARD + "/sales-graph"))
          .andExpect(status().isOk())
          .andExpect(content().contentType(MediaType.APPLICATION_JSON))
          .andExpect(jsonPath("$").isArray());
    }
  }
}
