package br.com.threadstech.stockfy.config.constraints;

import br.com.threadstech.stockfy.entity.Product;
import java.util.Map;

public class ProductConstraintNames implements ConstraintNames<Product> {
  public ProductConstraintNames() {}

  private static final String PREFIX = "uk_product_";
  public static final String UK_BAR_CODE = PREFIX + "bar_code";

  private static final Map<String, String> CONSTRAINT_TO_I18N_KEY = Map.of(UK_BAR_CODE, "barCode");

  @Override
  public String getI18nKeyMap(String rawConstraint) {
    return CONSTRAINT_TO_I18N_KEY.get(rawConstraint);
  }

  @Override
  public Class<Product> getI18nEntityClass() {
    return Product.class;
  }
}
