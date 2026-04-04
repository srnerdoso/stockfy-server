package br.com.threadstech.stockfy.modules.product;

import br.com.threadstech.stockfy.modules.product.dto.ProductCreateDto;
import br.com.threadstech.stockfy.modules.product.dto.ProductDetailsDto;
import br.com.threadstech.stockfy.modules.product.dto.ProductResponseV1_1;
import br.com.threadstech.stockfy.modules.product.dto.ProductSummaryDto;
import br.com.threadstech.stockfy.modules.product.enums.ProductResponseType;
import br.com.threadstech.stockfy.utils.SwaggerRefUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "Product")
public interface ProductV1_1ControllerDoc {

  @Operation(
      summary = "Busca todos os produtos",
      security = @SecurityRequirement(name = "jwt - Cookie HttpOnly"),
      parameters = {
        @Parameter(
            name = "name",
            in = ParameterIn.QUERY,
            description = "Nome do produto para filtro (case-insensitive)"),
        @Parameter(
            name = "type",
            in = ParameterIn.QUERY,
            required = true,
            description = "Tipo de resposta desejada (SUMMARY, SEARCH, SALE)"),
      },
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Lista de produtos paginada encontrada com sucesso.",
            content =
                @Content(
                    array =
                        @ArraySchema(
                            schema = @Schema(implementation = ProductResponseV1_1.class)))),
        @ApiResponse(responseCode = "400", ref = SwaggerRefUtils.BAD_REQUEST_RES),
        @ApiResponse(responseCode = "401", ref = SwaggerRefUtils.UNAUTHORIZED_RES),
        @ApiResponse(responseCode = "403", ref = SwaggerRefUtils.FORBIDDEN_RES),
      })
  ResponseEntity<Page<? extends ProductResponseV1_1>> findAll(
      @RequestParam(required = false) String name,
      @RequestParam ProductResponseType type,
      @ParameterObject Pageable pageable);

  @Operation(
      summary = "Salva um produto",
      security = @SecurityRequirement(name = "jwt - Cookie HttpOnly"),
      requestBody =
          @io.swagger.v3.oas.annotations.parameters.RequestBody(
              required = true,
              description = "Produto a ser salvo",
              content = @Content(schema = @Schema(implementation = ProductCreateDto.class))),
      responses = {
        @ApiResponse(responseCode = "201", description = "Produto criado com sucesso."),
        @ApiResponse(responseCode = "400", description = SwaggerRefUtils.BAD_REQUEST_RES),
        @ApiResponse(responseCode = "401", ref = SwaggerRefUtils.UNAUTHORIZED_RES),
        @ApiResponse(responseCode = "403", ref = SwaggerRefUtils.FORBIDDEN_RES),
        @ApiResponse(responseCode = "404", ref = SwaggerRefUtils.NOT_FOUND_RES),
      })
  ResponseEntity<Void> save(@Valid @RequestBody ProductCreateDto productDto);

  @Operation(
      summary = "Busca um produto por código de barras",
      security = @SecurityRequirement(name = "jwt - Cookie HttpOnly"),
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Produto encontrado com sucesso.",
            content = @Content(schema = @Schema(implementation = ProductDetailsDto.class))),
        @ApiResponse(responseCode = "401", ref = SwaggerRefUtils.UNAUTHORIZED_RES),
        @ApiResponse(responseCode = "403", ref = SwaggerRefUtils.FORBIDDEN_RES),
        @ApiResponse(responseCode = "404", ref = SwaggerRefUtils.NOT_FOUND_RES)
      })
  ResponseEntity<ProductDetailsDto> getByBarcode(@PathVariable String barcode);
}
