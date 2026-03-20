package br.com.threadstech.stockfy.api;

public class ApiPaths {

  private ApiPaths() {}

  public static final String API_BASE_V1 = "/api/v1";

  public static final String PRODUCT_V1 = API_BASE_V1 + "/products";

  public static final String PAYMENT_V1 = API_BASE_V1 + "/payments";

  public static final String CUSTOMER_V1 = API_BASE_V1 + "/customers";

  public static final String EMPLOYEE_V1 = API_BASE_V1 + "/employees";

  public static final String DASHBOARD_V1 = API_BASE_V1 + "/dashboard";

  public static final String AUTH_V1 = API_BASE_V1 + "/auth";
}
