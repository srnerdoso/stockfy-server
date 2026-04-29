package br.com.threadstech.stockfy.web.presentation;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.test.web.servlet.ResultActions;

public final class ApiErrorResponseAssertions {

  private ApiErrorResponseAssertions() {}

  public static void assertBadRequestFieldValidation(
      ResultActions result, String field, String message) throws Exception {
    result
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.type").value("about:blank"))
        .andExpect(jsonPath("$.title").value("Validation Error"))
        .andExpect(jsonPath("$.status").value(400))
        .andExpect(jsonPath("$.detail").value("Erro de validação nos campos informados."))
        .andExpect(jsonPath("$.instance").doesNotExist())
        .andExpect(jsonPath("$.fieldErrors.length()").value(1))
        .andExpect(jsonPath("$.fieldErrors[0].field").value(field))
        .andExpect(jsonPath("$.fieldErrors[0].message").value(message));
  }
}
