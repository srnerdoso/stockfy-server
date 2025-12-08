package br.com.threadstech.stockfy.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.annotations.OpenAPI30;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
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
                        .url("https://valdenorportifolio.vercel.app/")));
  }
}
