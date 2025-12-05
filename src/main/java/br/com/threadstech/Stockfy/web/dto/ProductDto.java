package br.com.threadstech.stockfy.web.dto;

import br.com.threadstech.stockfy.validation.ProductType;
import br.com.threadstech.stockfy.web.dto.groups.Create;
import br.com.threadstech.stockfy.web.dto.groups.Update;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

// TODO: Corrigir design do dto. Usar 2 DTOs separados (Create e Update) e remover groups deste dto
// TODO: Adicionar anotações de validação que estão faltando. Analisar a entidade e ver quais campos
// são obrigatórios
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ProductDto {

  @NotBlank(message = "{NotBlank.productDto.barCode}", groups = Create.class)
  @Size(
      min = 7,
      max = 20,
      message = "{Size.productDto.barCode}",
      groups = {Create.class, Update.class})
  private String barCode;

  @NotBlank(message = "{NotBlank.productDto.name}", groups = Create.class)
  @Size(
      min = 1,
      max = 255,
      message = "{Size.productDto.name}",
      groups = {Create.class, Update.class})
  private String name;

  @Positive(
      message = "{Positive.productDto.stock}",
      groups = {Create.class, Update.class})
  private BigDecimal stock;

  @Positive(
      message = "{Positive.productDto.price}",
      groups = {Create.class, Update.class})
  private BigDecimal price;

  @Positive(
      message = "{Positive.productDto.cost}",
      groups = {Create.class, Update.class})
  private BigDecimal cost;

  @Positive(
      message = "{Positive.productDto.profit}",
      groups = {Create.class, Update.class})
  private BigDecimal profit;

  @PositiveOrZero(
      message = "{PositiveOrZero.productDto.discount}",
      groups = {Create.class, Update.class})
  private BigDecimal discount;

  @PositiveOrZero(
      message = "{PositiveOrZero.productDto.discountPercentage}",
      groups = {Create.class, Update.class})
  private BigDecimal discountPercentage;

  @NotNull(message = "{NotNull.productDto.type}", groups = Create.class)
  @ProductType(
      message = "{ProductType.productDto.type}",
      groups = {Create.class, Update.class})
  private String type;
}
