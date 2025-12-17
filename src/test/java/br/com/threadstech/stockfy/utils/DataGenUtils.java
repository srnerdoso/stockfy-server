package br.com.threadstech.stockfy.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.SneakyThrows;
import net.datafaker.Faker;

@Getter
public class DataGenUtils {

  public static final Faker faker = new Faker();
  public static final ObjectMapper objectMapper = new ObjectMapper();

  @SneakyThrows
  public static String toJson(Object object) {
    return objectMapper.writeValueAsString(object);
  }
}
