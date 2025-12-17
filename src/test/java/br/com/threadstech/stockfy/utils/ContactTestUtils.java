package br.com.threadstech.stockfy.utils;

import br.com.threadstech.stockfy.web.dto.ContactCreateDto;
import br.com.threadstech.stockfy.web.dto.ContactUpdateDto;

public class ContactTestUtils {

  public static ContactCreateDto validContactCreateDto() {
    return ContactCreateDto.builder()
        .email(DataGenUtils.faker.internet().emailAddress())
        .phoneNumber(DataGenUtils.faker.phoneNumber().phoneNumberInternational())
        .build();
  }

  public static ContactCreateDto invalidContactCreateDto() {
    return ContactCreateDto.builder()
        .email("inavlid arroba email.com")
        .phoneNumber("123456789")
        .build();
  }

  public static ContactCreateDto nullFieldsContactCreateDto() {
    return new ContactCreateDto();
  }

  public static ContactUpdateDto validContactUpdateDto() {
    return ContactUpdateDto.builder()
        .email(DataGenUtils.faker.internet().emailAddress())
        .phoneNumber(DataGenUtils.faker.phoneNumber().phoneNumberInternational())
        .build();
  }

  public static ContactUpdateDto invalidContactUpdateDto() {
    return ContactUpdateDto.builder()
        .email("helloWorld arroba email.com")
        .phoneNumber("123456789")
        .build();
  }
}
