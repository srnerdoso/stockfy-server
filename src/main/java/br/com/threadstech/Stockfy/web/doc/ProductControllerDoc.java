package br.com.threadstech.stockfy.web.doc;

import br.com.threadstech.stockfy.utils.SwaggerRefUtils;
import br.com.threadstech.stockfy.web.dto.ProductCreateDto;
import br.com.threadstech.stockfy.web.dto.ProductResponseDto;
import br.com.threadstech.stockfy.web.dto.ProductUpdateDto;
import br.com.threadstech.stockfy.web.exception.ErrorMessage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Product")
public interface ProductControllerDoc {

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
  ResponseEntity<Void> save(@Valid @RequestBody ProductCreateDto product);

  @Operation(
      summary = "Busca todos os produtos",
      description = "Retorna uma lista de produtos paginada.",
      security = @SecurityRequirement(name = "jwt - Cookie HttpOnly"),
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Produtos encontrados com sucesso.",
            content =
                @Content(
                    schema =
                        @Schema(
                            implementation = ProductResponseDto.class,
                            contentMediaType = "application/json"))),
        @ApiResponse(responseCode = "401", ref = SwaggerRefUtils.UNAUTHORIZED_RES),
        @ApiResponse(responseCode = "403", ref = SwaggerRefUtils.FORBIDDEN_RES)
      })
  ResponseEntity<Page<ProductResponseDto>> findAll(@PageableDefault Pageable pageable);

  @Operation(
      summary = "Busca um produto por barcode",
      security = @SecurityRequirement(name = "jwt - Cookie HttpOnly"),
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Produto encontrado com sucesso.",
            content = @Content(schema = @Schema(implementation = ProductResponseDto.class))),
        @ApiResponse(responseCode = "401", ref = SwaggerRefUtils.UNAUTHORIZED_RES),
        @ApiResponse(responseCode = "403", ref = SwaggerRefUtils.FORBIDDEN_RES),
        @ApiResponse(responseCode = "404", ref = SwaggerRefUtils.NOT_FOUND_RES)
      })
  ResponseEntity<ProductResponseDto> findByBarCode(@PathVariable String barCode);

  @Operation(
      summary = "Busca um ou mais produtos por nome",
      description = "Retorna uma lista de produtos paginada.",
      security = @SecurityRequirement(name = "jwt - Cookie HttpOnly"),
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Produto encontrado com sucesso.",
            content =
                @Content(
                    schema =
                        @Schema(
                            implementation = ProductResponseDto.class,
                            contentMediaType = "application/json"))),
        @ApiResponse(responseCode = "401", ref = SwaggerRefUtils.UNAUTHORIZED_RES),
        @ApiResponse(responseCode = "403", ref = SwaggerRefUtils.FORBIDDEN_RES),
      })
  ResponseEntity<Page<ProductResponseDto>> findAllByName(
      @PathVariable String name, @PageableDefault Pageable pageable);

  @Operation(
      summary = "Atualiza um produto por id",
      security = @SecurityRequirement(name = "jwt - Cookie HttpOnly"),
      requestBody =
          @io.swagger.v3.oas.annotations.parameters.RequestBody(
              required = true,
              description = "Produto a ser atualizado",
              content = @Content(schema = @Schema(implementation = ProductUpdateDto.class))),
      responses = {
        @ApiResponse(responseCode = "204", description = "Produto atualizado com sucesso."),
        @ApiResponse(responseCode = "401", ref = SwaggerRefUtils.UNAUTHORIZED_RES),
        @ApiResponse(responseCode = "403", ref = SwaggerRefUtils.FORBIDDEN_RES),
        @ApiResponse(responseCode = "404", ref = SwaggerRefUtils.NOT_FOUND_RES),
        @ApiResponse(responseCode = "409", ref = SwaggerRefUtils.CONFLICT_RES),
      })
  ResponseEntity<Void> updateProductById(
      @PathVariable Long id, @Valid @RequestBody ProductUpdateDto productDto);

  @Operation(
      summary = "Deleta um produto por id",
      security = @SecurityRequirement(name = "jwt - Cookie HttpOnly"),
      responses = {
        @ApiResponse(responseCode = "204", description = "Produto deletado com sucesso.")
      })
  ResponseEntity<Void> deleteProductById(@PathVariable Long id);
}
