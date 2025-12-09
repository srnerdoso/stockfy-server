package br.com.threadstech.stockfy.web.doc;

import br.com.threadstech.stockfy.utils.SwaggerRefUtils;
import br.com.threadstech.stockfy.web.dto.LoginDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Authentication")
public interface AuthControllerDoc {

  @Operation(
      summary = "Autentica um usuário.",
      requestBody =
          @io.swagger.v3.oas.annotations.parameters.RequestBody(
              content =
                  @Content(
                      schema =
                          @Schema(
                              implementation = LoginDto.class,
                              contentMediaType = "application/json"))),
      responses = {
        @ApiResponse(responseCode = "204", description = "Autenticação realizada com sucesso."),
        @ApiResponse(responseCode = "400", ref = SwaggerRefUtils.BAD_REQUEST_RES),
        @ApiResponse(responseCode = "404", ref = SwaggerRefUtils.NOT_FOUND_RES),
      })
  ResponseEntity<Void> auth(@Valid @RequestBody LoginDto loginDto, HttpServletResponse response);

  @Operation(
      summary = "Atualiza o access token.",
      responses = {
        @ApiResponse(responseCode = "204", description = "Access token atualizado com sucesso."),
        @ApiResponse(responseCode = "404", ref = SwaggerRefUtils.NOT_FOUND_RES),
      })
  ResponseEntity<Void> refreshToken(HttpServletRequest request, HttpServletResponse response);

  @Operation(
      summary = "Desconecta um usuário.",
      responses = {
        @ApiResponse(responseCode = "204", description = "Usuário desconectado com sucesso."),
      })
  ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response);
}
