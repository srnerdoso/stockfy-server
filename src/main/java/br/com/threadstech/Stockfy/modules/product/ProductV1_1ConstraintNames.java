package br.com.threadstech.stockfy.modules.product;

import br.com.threadstech.stockfy.config.constraints.ConstraintNames;
import java.util.Map;

public class ProductV1_1ConstraintNames implements ConstraintNames<ProductV1_1> {
  public ProductV1_1ConstraintNames() {}

  public static final String UK_BARCODE = "uk_product_v1_1_barcode";

  private static final Map<String, String> CONSTRAINT_TO_I18N_KEY = Map.of(UK_BARCODE, "barCode");

  @Override
  public String getI18nKeyMap(String rawConstraint) {
    return CONSTRAINT_TO_I18N_KEY.get(rawConstraint);
  }

  @Override
  public Class<ProductV1_1> getI18nEntityClass() {
    return ProductV1_1.class;
  }
}
