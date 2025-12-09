package br.com.threadstech.stockfy.config;

import br.com.threadstech.stockfy.utils.SwaggerRefUtils;
import br.com.threadstech.stockfy.web.exception.ErrorMessage;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.responses.ApiResponse;
import java.util.Map;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SpringDocOpenApiConfig {

  @Bean
  public OpenAPI openApi() {
    return new OpenAPI()
        .info(
            new Info()
                .title("Stockfy API")
                .version("1.0.0")
                .description("Stockfy API")
                .contact(
                    new Contact()
                        .name("Valdenor")
                        .email("valdenor.filhov@gmail.com")
                        .url("https://valdenorportifolio.vercel.app/")))
        .components(
            new Components()
                .addResponses(
                    SwaggerRefUtils.BAD_REQUEST_RES, buildErrorMessageResponse("Campos inválidos."))
                .addResponses(
                    SwaggerRefUtils.UNAUTHORIZED_RES,
                    new ApiResponse().description("Acesso não permitido."))
                .addResponses(
                    SwaggerRefUtils.FORBIDDEN_RES,
                    new ApiResponse().description("Acesso não autorizado."))
                .addResponses(
                    SwaggerRefUtils.NOT_FOUND_RES,
                    buildErrorMessageResponse("Recurso não encontrado."))
                .addResponses(
                    SwaggerRefUtils.CONFLICT_RES,
                    buildErrorMessageResponse("Conflito entre os dados."))
                .addResponses(
                    SwaggerRefUtils.UNPROCESSABLE_ENTITY_RES,
                    buildErrorMessageResponse("Recurso não processável.")));
  }

  private ApiResponse buildErrorMessageResponse(String description) {
    String exampleString = "string";
    int exampleInt = 0;
    Map<String, String> exampleMap =
        Map.of(
            "additionalProp1", exampleString,
            "additionalProp2", exampleString,
            "additionalProp3", exampleString);
    ErrorMessage exampleErrorMessage =
        ErrorMessage.builder()
            .message(exampleString)
            .method(exampleString)
            .path(exampleString)
            .status(exampleInt)
            .statusText(exampleString)
            .errors(exampleMap)
            .build();

    return new ApiResponse()
        .description(description)
        .content(
            new Content()
                .addMediaType("application/json", new MediaType().example(exampleErrorMessage)));
  }
}
