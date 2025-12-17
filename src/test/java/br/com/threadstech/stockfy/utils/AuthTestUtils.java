package br.com.threadstech.stockfy.utils;

import br.com.threadstech.stockfy.api.ApiPaths;
import br.com.threadstech.stockfy.web.dto.LoginDto;

public class AuthTestUtils {

  // Dados de /sql/employees-insert.sql para Employee Id 100
  public static final long employeeId = 100L;
  public static final String email100 = "marcos.duarte91@example.com";
  public static final String password100 = "senhasegura1234";

  public static LoginDto validLoginDto() {
    return LoginDto.builder()
        .email(email100)
        .password(password100)
        .deviceId(DataGenUtils.faker.internet().uuid())
        .build();
  }

  public static String validLoginJson() {
    return DataGenUtils.toJson(validLoginDto());
  }

  public static String invalidLoginJson() {
    var dto =
        LoginDto.builder()
            .email("invalid@example.com")
            .password("senhaInvalidaEInsegura123456")
            .deviceId("invalidDeviceId")
            .build();
    return DataGenUtils.toJson(dto);
  }

  public static String blankFieldsLoginJson() {
    var dto = LoginDto.builder().email("").password("").deviceId("").build();
    return DataGenUtils.toJson(dto);
  }

  public static String nullFieldsLoginJson() {
    return DataGenUtils.toJson(new LoginDto());
  }

  public static String loginPath() {
    return patternPath("login");
  }

  public static String refreshPath() {
    return patternPath("refresh");
  }

  public static String logoutPath() {
    return patternPath("logout");
  }

  private static String patternPath(String path) {
    return ApiPaths.AUTH + "/" + path;
  }
}
