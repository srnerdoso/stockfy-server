package br.com.threadstech.stockfy.components;

import jakarta.servlet.http.Cookie;
import java.util.Arrays;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Getter
@Component
public class CookieUtils {

  private final String accessTokenCookieName = "access_token";
  private final String refreshTokenCookieName = "refresh_token";

  public Cookie createHttpOnlyCookie(String name, String value, int maxAge) {
    Cookie cookie = new Cookie(name, value);
    cookie.setHttpOnly(true);
    cookie.setPath("/");
    cookie.setMaxAge(maxAge);
    return cookie;
  }

  public void deleteCookie(String name, String value) {
    createHttpOnlyCookie(name, value, 0);
  }

  public Cookie getCookieByName(Cookie[] cookies, String expectedName) {
    if (cookies == null) {
      return null;
    }
    return Arrays.stream(cookies)
        .filter(cookie -> cookie.getName().equals(expectedName))
        .findFirst()
        .orElse(null);
  }
}
