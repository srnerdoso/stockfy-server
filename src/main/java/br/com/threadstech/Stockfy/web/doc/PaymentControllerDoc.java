package br.com.threadstech.stockfy.web.doc;

import br.com.threadstech.stockfy.utils.SwaggerRefUtils;
import br.com.threadstech.stockfy.web.dto.PaymentCreateDto;
import br.com.threadstech.stockfy.web.exception.ErrorMessage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Payment")
public interface PaymentControllerDoc {

  @Operation(
      summary = "Efetua um pagamento.",
      requestBody =
          @io.swagger.v3.oas.annotations.parameters.RequestBody(
              required = true,
              content = @Content(schema = @Schema(implementation = PaymentCreateDto.class))),
      responses = {
        @ApiResponse(responseCode = "201", description = "Pagamento efetuado com sucesso."),
        @ApiResponse(responseCode = "401", ref = SwaggerRefUtils.UNAUTHORIZED_RES),
        @ApiResponse(responseCode = "403", ref = SwaggerRefUtils.FORBIDDEN_RES),
        @ApiResponse(
            responseCode = "404",
            description = "Produto não encontrado",
            content = @Content(schema = @Schema(implementation = ErrorMessage.class)))
      })
  ResponseEntity<Void> pay(@Valid @RequestBody PaymentCreateDto paymentDto);

  @Operation(
      summary = "Reembolsa um pagamento.",
      responses = {
        @ApiResponse(responseCode = "204", description = "Pagamento reembolsado com sucesso."),
        @ApiResponse(responseCode = "401", ref = SwaggerRefUtils.UNAUTHORIZED_RES),
        @ApiResponse(responseCode = "403", ref = SwaggerRefUtils.FORBIDDEN_RES),
        @ApiResponse(
            responseCode = "404",
            description = "Produto não encontrado",
            content = @Content(schema = @Schema(implementation = ErrorMessage.class))),
        @ApiResponse(responseCode = "422", ref = SwaggerRefUtils.UNPROCESSABLE_ENTITY_RES)
      })
  ResponseEntity<Void> refund(@PathVariable Long id);
}
