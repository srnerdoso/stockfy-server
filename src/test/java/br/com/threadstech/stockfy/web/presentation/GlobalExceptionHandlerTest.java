package br.com.threadstech.stockfy.web.presentation;

import static org.junit.jupiter.api.Assertions.assertEquals;

import br.com.threadstech.stockfy.web.application.dto.ApiErrorResponse;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import java.util.List;
import java.util.Locale;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.StaticMessageSource;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mock.http.MockHttpInputMessage;

class GlobalExceptionHandlerTest {

  @Test
  @DisplayName("Deve traduzir mensagem de campo inválido usando MessageSource")
  void handleMessageNotReadable_whenFieldIsInvalid_thenUsesMessageSource() {
    StaticMessageSource messageSource = new StaticMessageSource();
    messageSource.addMessage("validation.error.message", Locale.getDefault(), "Erro traduzido.");
    messageSource.addMessage("validation.field.required", Locale.getDefault(), "Campo traduzido.");
    GlobalExceptionHandler handler = new GlobalExceptionHandler(messageSource);
    InvalidFormatException cause =
        InvalidFormatException.from(null, "Invalid value", "INVALID", String.class);
    cause.prependPath(new Object(), "role");
    HttpMessageNotReadableException exception =
        new HttpMessageNotReadableException(
            "Invalid JSON", cause, new MockHttpInputMessage(new byte[0]));

    ApiErrorResponse body = handler.handleMessageNotReadable(exception).getBody();

    assertEquals("Erro traduzido.", body.getDetail());
    assertEquals("role", body.getFieldErrors().getFirst().field());
    assertEquals("Campo traduzido.", body.getFieldErrors().getFirst().message());
  }

  @Test
  @DisplayName("Deve expor erros de campo sem depender de FieldError do Spring")
  void constructor_whenFieldErrorsAreProvided_thenUsesApiFieldError() {
    ApiErrorResponse response =
        new ApiErrorResponse(
            "about:blank",
            "Validation Error",
            400,
            "Erro traduzido.",
            null,
            List.of(new ApiErrorResponse.FieldError("role", "Campo traduzido.")));

    assertEquals("role", response.getFieldErrors().getFirst().field());
    assertEquals("Campo traduzido.", response.getFieldErrors().getFirst().message());
  }
}
