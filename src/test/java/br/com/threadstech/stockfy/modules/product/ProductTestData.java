package br.com.threadstech.stockfy.modules.product;

import java.math.BigDecimal;
import java.util.UUID;

public class ProductTestData {

  public static final String BARCODE = UUID.randomUUID().toString();
  public static final String NAME = "Product 1";
  public static final Integer STOCK = 10;
  public static final Integer MIN_STOCK = 5;
  public static final BigDecimal PRICE = new BigDecimal("100.00");
  public static final BigDecimal COST = new BigDecimal("50.00");
  public static final BigDecimal DISCOUNT = new BigDecimal("10.00");
  public static final String TYPE = "ELECTRONICS";

  public static final String VALID_CREATE_JSON = """
      {
          "barcode": "%s",
          "name": "%s",
          "stock": %d,
          "minStock": %d,
          "price": %s,
          "cost": %s,
          "discount": %s,
          "type": "%s"
      }
      """.formatted(BARCODE, NAME, STOCK, MIN_STOCK, PRICE, COST, DISCOUNT, TYPE);

  public static final String INVALID_JSON_PRICE_LESS_THAN_COST = """
      {
          "barcode": "%s",
          "name": "%s",
          "stock": %d,
          "minStock": %d,
          "price": 40.00,
          "cost": 50.00,
          "discount": 0.00,
          "type": "%s"
      }
      """.formatted(BARCODE, NAME, STOCK, MIN_STOCK, TYPE);

  public static final String INVALID_JSON_DISCOUNT_GREATER_THAN_PRICE = """
      {
          "barcode": "%s",
          "name": "%s",
          "stock": %d,
          "minStock": %d,
          "price": 100.00,
          "cost": 50.00,
          "discount": 110.00,
          "type": "%s"
      }
      """.formatted(BARCODE, NAME, STOCK, MIN_STOCK, TYPE);

  public static final String INVALID_JSON_MISSING_REQUIRED = """
      {
          "barcode": null,
          "name": null,
          "stock": -1,
          "minStock": -1,
          "price": -10.00,
          "cost": -5.00,
          "type": null
      }
      """;
}
