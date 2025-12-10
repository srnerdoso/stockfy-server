package br.com.threadstech.stockfy.utils;

import br.com.threadstech.stockfy.enums.ProductType;
import br.com.threadstech.stockfy.web.dto.ProductCreateDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import net.datafaker.Faker;

import java.math.BigDecimal;

public class DataGenUtils {

  private static final Faker faker = new Faker();
  private static final ObjectMapper objectMapper = new ObjectMapper();

  private static final BigDecimal randomPrecision10Scale3 =
      new BigDecimal(randomDecimalString(10, 3));
  private static final BigDecimal randomPrecision10Scale2 =
      new BigDecimal(randomDecimalString(10, 2));
  private static final BigDecimal randomPrecision5Scale2 =
      new BigDecimal(randomDecimalString(5, 2));

  @SneakyThrows
  public static String validProductCreateJson() {

    String barCode = String.valueOf(faker.barcode().ean13());
    String name = faker.commerce().productName();
    String type = faker.options().option(ProductType.class).name();

    var productDto = ProductCreateDto.builder()
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

    return objectMapper.writeValueAsString(productDto);
  }

  private static String randomDecimalString(int precision, int scale) {
    int integerDigits = precision - scale;

    long maxInteger = (long) Math.pow(10, integerDigits);
    long integer = faker.number().numberBetween(1, maxInteger);

    int decimalMax = (int) Math.pow(10, scale);
    long decimal = faker.number().numberBetween(0, decimalMax);
    return integer + "." + String.format("%0" + scale + "d", decimal);
  }
}
