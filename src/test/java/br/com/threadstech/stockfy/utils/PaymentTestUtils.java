package br.com.threadstech.stockfy.utils;

import br.com.threadstech.stockfy.api.ApiPaths;
import br.com.threadstech.stockfy.enums.PaymentMethod;
import br.com.threadstech.stockfy.enums.PaymentStatus;
import br.com.threadstech.stockfy.web.dto.CartCreateDto;
import br.com.threadstech.stockfy.web.dto.PaymentCreateDto;
import java.math.BigDecimal;
import java.util.Set;

public class PaymentTestUtils {

  public static PaymentCreateDto validPaymentCreateDto(
      PaymentMethod paymentMethod, PaymentStatus paymentStatus) {
    return PaymentCreateDto.builder()
        .customerId(2L)
        .paymentMethod(paymentMethod.name())
        .paymentStatus(paymentStatus.name())
        .cart(
            Set.of(
                CartCreateDto.builder()
                    .productId(1L)
                    .quantity(BigDecimal.valueOf(DataGenUtils.faker.number().numberBetween(1, 10)))
                    .paymentValue(
                        BigDecimal.valueOf(DataGenUtils.faker.number().randomDouble(2, 10, 500)))
                    .build()))
        .total(BigDecimal.valueOf(DataGenUtils.faker.number().randomDouble(2, 10, 500)))
        .build();
  }

  public static String invalidPaymentCreateJson() {
    var dto =
        PaymentCreateDto.builder()
            .customerId(-1L)
            .paymentMethod("")
            .paymentStatus("ANY_STATUS")
            .cart(null)
            .total(BigDecimal.valueOf(-10))
            .build();
    return DataGenUtils.toJson(dto);
  }

  public static String nullFieldsPaymentCreateJson() {
    return DataGenUtils.toJson(new PaymentCreateDto());
  }

  public static String patternPath(String path) {
    return ApiPaths.PAYMENT + "/" + path;
  }

  public static String patternPath(String path, String resource) {
    return ApiPaths.PAYMENT + "/" + path + "/" + resource;
  }

  public static String payPath() {
    return patternPath("/pay");
  }

  public static String refundPath(Long id) {
    return patternPath(id.toString(), "refund");
  }
}
