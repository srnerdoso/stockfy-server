package br.com.threadstech.stockfy.config.constraints;

import br.com.threadstech.stockfy.entity.Customer;
import br.com.threadstech.stockfy.entity.Employee;
import java.util.Map;

public class EmployeeConstraintNames implements ConstraintNames<Employee> {
  public EmployeeConstraintNames() {}

  private static final String PREFIX = "uk_employee_";
  public static final String UK_CPF = PREFIX + "cpf";
  public static final String UK_EMAIL = PREFIX + "email";
  public static final String UK_PHONE_NUMBER = PREFIX + "phone_number";

  private static final Map<String, String> CONSTRAINT_TO_I18N_KEY =
      Map.of(
          UK_CPF, "cpf",
          UK_EMAIL, "email",
          UK_PHONE_NUMBER, "phoneNumber");

  @Override
  public String getI18nKeyMap(String rawConstraint) {
    return CONSTRAINT_TO_I18N_KEY.get(rawConstraint);
  }

  @Override
  public Class<Employee> getI18nEntityClass() {
    return Employee.class;
  }
}
