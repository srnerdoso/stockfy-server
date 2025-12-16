package br.com.threadstech.stockfy.utils;

import br.com.threadstech.stockfy.enums.ProductType;
import br.com.threadstech.stockfy.web.dto.ProductCreateDto;
import br.com.threadstech.stockfy.web.dto.ProductUpdateDto;
import lombok.SneakyThrows;

import java.math.BigDecimal;

public class ProductTestsUtils {

  private static final BigDecimal randomPrecision10Scale3 =
      new BigDecimal(randomDecimalString(10, 3));
  private static final BigDecimal randomPrecision10Scale2 =
      new BigDecimal(randomDecimalString(10, 2));
  private static final BigDecimal randomPrecision5Scale2 =
      new BigDecimal(randomDecimalString(5, 2));
  private static final BigDecimal negativeScale = new BigDecimal("-1.000");

  @SneakyThrows
  public static ProductCreateDto validProductCreateDto(String name) {
    String barCode = String.valueOf(DataGenUtils.faker.barcode().ean13());
    String type = DataGenUtils.faker.options().option(ProductType.class).name();

    return ProductCreateDto.builder()
        .barCode(barCode)
        .name(name)
        .stock(randomPrecision10Scale3)
        .price(randomPrecision10Scale2)
        .cost(randomPrecision10Scale2)
        .profit(randomPrecision10Scale2)
        .discount(randomPrecision10Scale2)
        .discountPercentage(randomPrecision5Scale2)
        .type(type)
        .build();
  }

  @SneakyThrows
  public static ProductUpdateDto validProductUpdateDto() {
    String barCode = String.valueOf(DataGenUtils.faker.barcode().ean13());
    String type = DataGenUtils.faker.options().option(ProductType.class).name();

    return ProductUpdateDto.builder()
        .barCode(barCode)
        .name(DataGenUtils.faker.commerce().productName())
        .stock(randomPrecision10Scale3)
        .price(randomPrecision10Scale2)
        .cost(randomPrecision10Scale2)
        .profit(randomPrecision10Scale2)
        .discount(randomPrecision10Scale2)
        .discountPercentage(randomPrecision5Scale2)
        .type(type)
        .build();
  }

  @SneakyThrows
  public static ProductCreateDto validProductCreateDto() {
    return validProductCreateDto(DataGenUtils.faker.commerce().productName());
  }

  @SneakyThrows
  public static String validProductCreateJson() {
    return DataGenUtils.toJson(validProductCreateDto());
  }

  @SneakyThrows
  public static String nullFieldsProductCreateJson() {
    return DataGenUtils.toJson(new ProductCreateDto());
  }

  @SneakyThrows
  public static String invalidSizeProductCreateJson() {
    return DataGenUtils.toJson(
        ProductCreateDto.builder()
            .barCode("123")
            .name("")
            .stock(negativeScale)
            .price(negativeScale)
            .cost(negativeScale)
            .profit(negativeScale)
            .discount(negativeScale)
            .discountPercentage(negativeScale)
            .type("ANY")
            .build());
  }

  private static String randomDecimalString(int precision, int scale) {
    int integerDigits = precision - scale;

    long maxInteger = (long) Math.pow(10, integerDigits);
    long integer = DataGenUtils.faker.number().numberBetween(1, maxInteger);

    int decimalMax = (int) Math.pow(10, scale);
    long decimal = DataGenUtils.faker.number().numberBetween(0, decimalMax);
    return integer + "." + String.format("%0" + scale + "d", decimal);
  }

  public static String validProductUpdateJson() {
    return DataGenUtils.toJson(validProductUpdateDto());
  }
}
