package br.com.threadstech.stockfy.utils;

import br.com.threadstech.stockfy.web.dto.EmployeeCreateDto;
import br.com.threadstech.stockfy.web.dto.EmployeeUpdateDto;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.LocalDate;
import java.time.ZoneId;

public class EmployeeTestsUtils {

  public static EmployeeCreateDto validEmployeeCreateDto() {
    registerJavaTimeModule();

    return EmployeeCreateDto.builder()
        .fullName(DataGenUtils.faker.name().fullName())
        .cpf(DataGenUtils.faker.cpf().valid())
        .birthday(DataGenUtils.faker.timeAndDate().birthday())
        .contact(ContactTestUtils.validContactCreateDto())
        .address(AddressTestUtils.validAddressCreateDto())
        .build();
  }

  public static String validEmployeeCreateJson() {
    return DataGenUtils.toJson(validEmployeeCreateDto());
  }

  public static String invalidEmployeeCreateJson() {
    registerJavaTimeModule();
    var dto =
        EmployeeCreateDto.builder()
            .fullName("")
            .cpf(DataGenUtils.faker.cpf().invalid())
            .birthday(
                LocalDate.ofInstant(
                    DataGenUtils.faker.timeAndDate().future(), ZoneId.systemDefault()))
            .contact(ContactTestUtils.invalidContactCreateDto())
            .address(AddressTestUtils.invalidAddressCreateDto())
            .build();
    return DataGenUtils.toJson(dto);
  }

  public static String addressContactNullFieldsEmployeeCreateDto() {
    registerJavaTimeModule();
    var dto =
        EmployeeCreateDto.builder()
            .fullName(DataGenUtils.faker.name().fullName())
            .cpf(DataGenUtils.faker.cpf().valid())
            .birthday(DataGenUtils.faker.timeAndDate().birthday())
            .contact(ContactTestUtils.nullFieldsContactCreateDto())
            .address(AddressTestUtils.nullFieldsAddressCreateDto())
            .build();
    return DataGenUtils.toJson(dto);
  }

  public static String nullFieldsEmployeeCreateJson() {
    return DataGenUtils.toJson(new EmployeeCreateDto());
  }

  public static EmployeeUpdateDto validEmployeeUpdateDto() {
    registerJavaTimeModule();

    return EmployeeUpdateDto.builder()
        .fullName(DataGenUtils.faker.name().fullName())
        .contact(ContactTestUtils.validContactUpdateDto())
        .address(AddressTestUtils.validAddressUpdateDto())
        .build();
  }

  public static String invalidEmployeeUpdateJson() {
    registerJavaTimeModule();
    var dto =
        EmployeeUpdateDto.builder()
            .fullName("")
            .contact(ContactTestUtils.invalidContactUpdateDto())
            .address(AddressTestUtils.invalidAddressUpdateDto())
            .build();
    return DataGenUtils.toJson(dto);
  }

  private static void registerJavaTimeModule() {
    DataGenUtils.objectMapper.registerModule(new JavaTimeModule());
  }

  public static String validEmployeeUpdateJson() {
    return DataGenUtils.toJson(validEmployeeUpdateDto());
  }
}
