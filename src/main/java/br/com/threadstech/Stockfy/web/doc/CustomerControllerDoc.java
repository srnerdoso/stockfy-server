package br.com.threadstech.stockfy.web.doc;

import br.com.threadstech.stockfy.utils.SwaggerRefUtils;
import br.com.threadstech.stockfy.web.dto.CustomerCreateDto;
import br.com.threadstech.stockfy.web.dto.CustomerDetailDto;
import br.com.threadstech.stockfy.web.dto.CustomerSummaryDto;
import br.com.threadstech.stockfy.web.dto.CustomerUpdateDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Customer")
public interface CustomerControllerDoc {

  @Operation(
      summary = "Cria um novo cliente.",
      requestBody =
          @io.swagger.v3.oas.annotations.parameters.RequestBody(
              content =
                  @Content(
                      schema =
                          @Schema(
                              implementation = CustomerCreateDto.class,
                              contentMediaType = "application/json"))),
      responses = {
        @ApiResponse(responseCode = "201", description = "Cliente criado com sucesso."),
        @ApiResponse(responseCode = "400", ref = SwaggerRefUtils.BAD_REQUEST_RES),
        @ApiResponse(responseCode = "401", ref = SwaggerRefUtils.UNAUTHORIZED_RES),
        @ApiResponse(responseCode = "403", ref = SwaggerRefUtils.FORBIDDEN_RES),
        @ApiResponse(responseCode = "404", ref = SwaggerRefUtils.NOT_FOUND_RES),
        @ApiResponse(responseCode = "409", ref = SwaggerRefUtils.CONFLICT_RES),
      })
  ResponseEntity<Void> save(@Valid @RequestBody CustomerCreateDto customerDto);

  @Operation(
      summary = "Busca e retorna uma lista de clientes paginada.",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Lista de clientes paginada.",
            content =
                @Content(
                    schema =
                        @Schema(
                            implementation = CustomerSummaryDto.class,
                            contentMediaType = "application/json"))),
        @ApiResponse(responseCode = "401", ref = SwaggerRefUtils.UNAUTHORIZED_RES),
        @ApiResponse(responseCode = "403", ref = SwaggerRefUtils.FORBIDDEN_RES),
      })
  ResponseEntity<Page<CustomerSummaryDto>> findAll(@PageableDefault Pageable pageable);

  @Operation(
      summary = "Busca um cliente pelo seu id.",
      responses = {
        @ApiResponse(responseCode = "200", description = "Cliente encontrado com sucesso."),
        @ApiResponse(responseCode = "401", ref = SwaggerRefUtils.UNAUTHORIZED_RES),
        @ApiResponse(responseCode = "403", ref = SwaggerRefUtils.FORBIDDEN_RES),
        @ApiResponse(responseCode = "404", ref = SwaggerRefUtils.NOT_FOUND_RES),
      })
  ResponseEntity<CustomerDetailDto> findById(@PathVariable Long id);

  @Operation(
      summary = "Retorna um cliente pelo seu cpf.",
      responses = {
        @ApiResponse(responseCode = "200", description = "Cliente encontrado com sucesso."),
        @ApiResponse(responseCode = "401", ref = SwaggerRefUtils.UNAUTHORIZED_RES),
        @ApiResponse(responseCode = "403", ref = SwaggerRefUtils.FORBIDDEN_RES),
        @ApiResponse(responseCode = "404", ref = SwaggerRefUtils.NOT_FOUND_RES),
      })
  ResponseEntity<String> findCpfById(@PathVariable Long id);

  @Operation(
      summary = "Atualiza um cliente pelo seu id.",
      responses = {
        @ApiResponse(responseCode = "204", description = "Cliente atualizado com sucesso."),
        @ApiResponse(responseCode = "401", ref = SwaggerRefUtils.UNAUTHORIZED_RES),
        @ApiResponse(responseCode = "403", ref = SwaggerRefUtils.FORBIDDEN_RES),
        @ApiResponse(responseCode = "404", ref = SwaggerRefUtils.NOT_FOUND_RES),
        @ApiResponse(responseCode = "409", ref = SwaggerRefUtils.CONFLICT_RES),
      })
  ResponseEntity<Void> update(
      @PathVariable Long id, @Valid @RequestBody CustomerUpdateDto customerDto);

  @Operation(
      summary = "Deleta um cliente pelo seu id.",
      responses = {
        @ApiResponse(responseCode = "204", description = "Cliente deletado com sucesso.")
      })
  ResponseEntity<Void> delete(@PathVariable Long id);
}
