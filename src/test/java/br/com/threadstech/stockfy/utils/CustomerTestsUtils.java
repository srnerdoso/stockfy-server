package br.com.threadstech.stockfy.utils;

import br.com.threadstech.stockfy.web.dto.CustomerCreateDto;
import br.com.threadstech.stockfy.web.dto.CustomerUpdateDto;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.LocalDate;
import java.time.ZoneId;

public class CustomerTestsUtils {

  public static CustomerCreateDto validCustomerCreateDto() {
    registerJavaTimeModule();

    return CustomerCreateDto.builder()
        .fullName(DataGenUtils.faker.name().fullName())
        .cpf(DataGenUtils.faker.cpf().valid())
        .birthday(DataGenUtils.faker.timeAndDate().birthday())
        .contact(ContactTestUtils.validContactCreateDto())
        .address(AddressTestUtils.validAddressCreateDto())
        .build();
  }

  public static String validCustomerCreateJson() {
    return DataGenUtils.toJson(validCustomerCreateDto());
  }

  public static String invalidCustomerCreateJson() {
    registerJavaTimeModule();
    var dto =
        CustomerCreateDto.builder()
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

  public static String addressContactNullFieldsCustomerCreateDto() {
    registerJavaTimeModule();
    var dto =
        CustomerCreateDto.builder()
            .fullName(DataGenUtils.faker.name().fullName())
            .cpf(DataGenUtils.faker.cpf().valid())
            .birthday(DataGenUtils.faker.timeAndDate().birthday())
            .contact(ContactTestUtils.nullFieldsContactCreateDto())
            .address(AddressTestUtils.nullFieldsAddressCreateDto())
            .build();
    return DataGenUtils.toJson(dto);
  }

  public static String nullFieldsCustomerCreateJson() {
    return DataGenUtils.toJson(new CustomerCreateDto());
  }

  public static CustomerUpdateDto validCustomerUpdateDto() {
    registerJavaTimeModule();

    return CustomerUpdateDto.builder()
        .fullName(DataGenUtils.faker.name().fullName())
        .contact(ContactTestUtils.validContactUpdateDto())
        .address(AddressTestUtils.validAddressUpdateDto())
        .build();
  }

  public static String invalidCustomerUpdateJson() {
    registerJavaTimeModule();
    var dto =
        CustomerUpdateDto.builder()
            .fullName("")
            .contact(ContactTestUtils.invalidContactUpdateDto())
            .address(AddressTestUtils.invalidAddressUpdateDto())
            .build();
    return DataGenUtils.toJson(dto);
  }

  private static void registerJavaTimeModule() {
    DataGenUtils.objectMapper.registerModule(new JavaTimeModule());
  }

  public static String validCustomerUpdateJson() {
    return DataGenUtils.toJson(validCustomerUpdateDto());
  }
}
