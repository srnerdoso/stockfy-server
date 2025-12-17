package br.com.threadstech.stockfy.utils;

import br.com.threadstech.stockfy.validation.AddressCreateDto;
import br.com.threadstech.stockfy.web.dto.AddressUpdateDto;
import net.datafaker.providers.base.Address;

public class AddressTestUtils {

  private static final Address fakerAddress = DataGenUtils.faker.address();

  public static AddressCreateDto validAddressCreateDto() {
    return AddressCreateDto.builder()
        .neighborhood(fakerAddress.streetPrefix())
        .complement(DataGenUtils.faker.name().firstName())
        .city(fakerAddress.city())
        .number(fakerAddress.buildingNumber())
        .street(fakerAddress.streetAddress())
        .country(fakerAddress.country())
        .state(fakerAddress.state())
        .zipCode(fakerAddress.zipCode())
        .build();
  }

  public static AddressCreateDto invalidAddressCreateDto() {
    return AddressCreateDto.builder()
        .neighborhood("")
        .complement("")
        .city("")
        .number("")
        .street("")
        .country("")
        .state("")
        .zipCode("")
        .build();
  }

  public static AddressCreateDto nullFieldsAddressCreateDto() {
    return new AddressCreateDto();
  }

  public static AddressUpdateDto validAddressUpdateDto() {
    return AddressUpdateDto.builder()
        .neighborhood(fakerAddress.streetPrefix())
        .complement(DataGenUtils.faker.name().firstName())
        .city(fakerAddress.city())
        .number(fakerAddress.buildingNumber())
        .street(fakerAddress.streetAddress())
        .country(fakerAddress.country())
        .state(fakerAddress.state())
        .zipCode(fakerAddress.zipCode())
        .build();
  }

  public static AddressUpdateDto invalidAddressUpdateDto() {
    return AddressUpdateDto.builder()
        .neighborhood("")
        .complement("")
        .city("")
        .number("")
        .street("")
        .country("")
        .state("")
        .zipCode("")
        .build();
  }
}
