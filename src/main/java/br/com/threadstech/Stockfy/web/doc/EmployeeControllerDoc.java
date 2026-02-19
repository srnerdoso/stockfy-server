package br.com.threadstech.stockfy.web.doc;

import br.com.threadstech.stockfy.utils.SwaggerRefUtils;
import br.com.threadstech.stockfy.web.dto.*;
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

@Tag(name = "Employee")
public interface EmployeeControllerDoc {

  @Operation(
      summary = "Cria uma conta de funcionário.",
      requestBody =
          @io.swagger.v3.oas.annotations.parameters.RequestBody(
              content =
                  @Content(
                      schema =
                          @Schema(
                              implementation = EmployeeCreateDto.class))),
      responses = {
        @ApiResponse(responseCode = "201", description = "Funcionário criado com sucesso."),
        @ApiResponse(responseCode = "400", ref = SwaggerRefUtils.BAD_REQUEST_RES),
        @ApiResponse(responseCode = "401", ref = SwaggerRefUtils.UNAUTHORIZED_RES),
        @ApiResponse(responseCode = "403", ref = SwaggerRefUtils.FORBIDDEN_RES),
        @ApiResponse(responseCode = "404", ref = SwaggerRefUtils.NOT_FOUND_RES),
        @ApiResponse(responseCode = "409", ref = SwaggerRefUtils.CONFLICT_RES),
      })
  ResponseEntity<Void> save(@Valid @RequestBody EmployeeCreateDto employeeCreateDto);

  @Operation(
      summary = "Busca todos os funcionários.",
      description = "Retorna uma lista de funcionários paginada.",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Lista de funcionários retornada com sucesso.",
            content =
                @Content(
                    schema =
                        @Schema(
                            implementation = EmployeeSummaryDto.class,
                            contentMediaType = "application/json"))),
        @ApiResponse(responseCode = "401", ref = SwaggerRefUtils.UNAUTHORIZED_RES),
        @ApiResponse(responseCode = "403", ref = SwaggerRefUtils.FORBIDDEN_RES)
      })
  ResponseEntity<Page<EmployeeSummaryDto>> findAll(@PageableDefault Pageable pageable);

  @Operation(
      summary = "Busca um funcionário por ID.",
      description = "Retorna um funcionário.",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Funcionário retornado com sucesso.",
            content =
                @Content(
                    schema =
                        @Schema(
                            implementation = EmployeeDetailDto.class,
                            contentMediaType = "application/json"))),
        @ApiResponse(responseCode = "401", ref = SwaggerRefUtils.UNAUTHORIZED_RES),
        @ApiResponse(responseCode = "403", ref = SwaggerRefUtils.FORBIDDEN_RES),
        @ApiResponse(responseCode = "404", ref = SwaggerRefUtils.NOT_FOUND_RES),
      })
  ResponseEntity<EmployeeDetailDto> findById(@PathVariable Long id);

  @Operation(
      summary = "Retorna um funcionário pelo seu cpf.",
      responses = {
        @ApiResponse(responseCode = "200", description = "Funcionário encontrado com sucesso."),
        @ApiResponse(responseCode = "401", ref = SwaggerRefUtils.UNAUTHORIZED_RES),
        @ApiResponse(responseCode = "403", ref = SwaggerRefUtils.FORBIDDEN_RES),
        @ApiResponse(responseCode = "404", ref = SwaggerRefUtils.NOT_FOUND_RES),
      })
  ResponseEntity<String> findCpfById(@PathVariable Long id);

  @Operation(
      summary = "Atualiza um funcionário pelo seu id.",
      responses = {
        @ApiResponse(responseCode = "204", description = "Funcionário atualizado com sucesso."),
        @ApiResponse(responseCode = "400", ref = SwaggerRefUtils.BAD_REQUEST_RES),
        @ApiResponse(responseCode = "401", ref = SwaggerRefUtils.UNAUTHORIZED_RES),
        @ApiResponse(responseCode = "403", ref = SwaggerRefUtils.FORBIDDEN_RES),
        @ApiResponse(responseCode = "404", ref = SwaggerRefUtils.NOT_FOUND_RES),
        @ApiResponse(responseCode = "409", ref = SwaggerRefUtils.CONFLICT_RES),
      })
  ResponseEntity<Void> updateById(
      @PathVariable Long id, @Valid @RequestBody EmployeeUpdateDto employeeUpdateDto);

  @Operation(
      summary = "Atualiza a senha de um funcionário pelo seu id.",
      responses = {
        @ApiResponse(responseCode = "204", description = "Senha atualizada com sucesso."),
        @ApiResponse(responseCode = "400", ref = SwaggerRefUtils.BAD_REQUEST_RES),
        @ApiResponse(responseCode = "401", ref = SwaggerRefUtils.UNAUTHORIZED_RES),
        @ApiResponse(responseCode = "403", ref = SwaggerRefUtils.FORBIDDEN_RES),
        @ApiResponse(responseCode = "404", ref = SwaggerRefUtils.NOT_FOUND_RES),
        @ApiResponse(responseCode = "422", ref = SwaggerRefUtils.UNPROCESSABLE_ENTITY_RES),
      })
  ResponseEntity<Void> updatePasswordById(
      @PathVariable Long id, @Valid @RequestBody PasswordUpdateDto passwordUpdateDto);

  @Operation(
      summary = "Deleta um funcionário pelo seu id.",
      responses = {
        @ApiResponse(responseCode = "204", description = "Funcionário deletado com sucesso.")
      })
  ResponseEntity<Void> deleteById(@PathVariable Long id);
}
