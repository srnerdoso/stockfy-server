package br.com.threadstech.stockfy.web.doc;

import br.com.threadstech.stockfy.enums.ProductType;
import br.com.threadstech.stockfy.utils.SwaggerRefUtils;
import br.com.threadstech.stockfy.web.dto.AlertDto;
import br.com.threadstech.stockfy.web.dto.AuditDto;
import br.com.threadstech.stockfy.web.dto.LastSaleDto;
import br.com.threadstech.stockfy.web.dto.MetricResponseDto;
import br.com.threadstech.stockfy.web.dto.SalesGraphDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "Dashboard")
public interface DashboardControllerDoc {

  @Operation(
      summary = "Busca métricas do dashboard",
      description = "Retorna uma lista de métricas incluindo faturamento, lucro e novos clientes.",
      security = @SecurityRequirement(name = "jwt - Cookie HttpOnly"),
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Métricas encontradas com sucesso.",
            content =
                @Content(
                    array = @ArraySchema(schema = @Schema(implementation = MetricResponseDto.class)))),
        @ApiResponse(responseCode = "401", ref = SwaggerRefUtils.UNAUTHORIZED_RES),
        @ApiResponse(responseCode = "403", ref = SwaggerRefUtils.FORBIDDEN_RES)
      })
  ResponseEntity<List<MetricResponseDto>> getMetrics();

  @Operation(
      summary = "Busca dados para o gráfico de vendas",
      description = "Retorna uma lista de dados de vendas filtrados por período (days, month, year).",
      security = @SecurityRequirement(name = "jwt - Cookie HttpOnly"),
      parameters = {
        @Parameter(
            name = "filter",
            description = "Filtro de período",
            schema = @Schema(allowableValues = {"days", "month", "year"}, defaultValue = "days"))
      },
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Dados do gráfico encontrados com sucesso.",
            content =
                @Content(
                    array = @ArraySchema(schema = @Schema(implementation = SalesGraphDto.class)))),
        @ApiResponse(responseCode = "401", ref = SwaggerRefUtils.UNAUTHORIZED_RES),
        @ApiResponse(responseCode = "403", ref = SwaggerRefUtils.FORBIDDEN_RES)
      })
  ResponseEntity<List<SalesGraphDto>> getSalesGraph(@RequestParam(name = "filter") String filter);


  @Operation(
      summary = "Busca as últimas vendas",
      description = "Retorna uma lista das vendas mais recentes realizadas no sistema.",
      security = @SecurityRequirement(name = "jwt - Cookie HttpOnly"),
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Últimas vendas encontradas com sucesso.",
            content =
                @Content(
                    array = @ArraySchema(schema = @Schema(implementation = LastSaleDto.class)))),
        @ApiResponse(responseCode = "401", ref = SwaggerRefUtils.UNAUTHORIZED_RES),
        @ApiResponse(responseCode = "403", ref = SwaggerRefUtils.FORBIDDEN_RES)
      })
  ResponseEntity<List<LastSaleDto>> getLastSales();

  @Operation(
      summary = "Busca logs de auditoria",
      description = "Retorna uma lista de eventos de auditoria do sistema.",
      security = @SecurityRequirement(name = "jwt - Cookie HttpOnly"),
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Logs de auditoria encontrados com sucesso.",
            content =
                @Content(
                    array = @ArraySchema(schema = @Schema(implementation = AuditDto.class)))),
        @ApiResponse(responseCode = "401", ref = SwaggerRefUtils.UNAUTHORIZED_RES),
        @ApiResponse(responseCode = "403", ref = SwaggerRefUtils.FORBIDDEN_RES)
      })
  ResponseEntity<List<AuditDto>> getAuditEvents();

  @Operation(
      summary = "Busca alertas do sistema",
      description = "Retorna uma lista de alertas, como estoque baixo.",
      security = @SecurityRequirement(name = "jwt - Cookie HttpOnly"),
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Alertas encontrados com sucesso.",
            content =
                @Content(
                    array = @ArraySchema(schema = @Schema(implementation = AlertDto.class)))),
        @ApiResponse(responseCode = "401", ref = SwaggerRefUtils.UNAUTHORIZED_RES),
        @ApiResponse(responseCode = "403", ref = SwaggerRefUtils.FORBIDDEN_RES)
      })
  ResponseEntity<List<AlertDto>> getAlerts();
}
