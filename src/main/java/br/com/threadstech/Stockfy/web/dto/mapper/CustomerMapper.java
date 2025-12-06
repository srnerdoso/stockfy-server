package br.com.threadstech.stockfy.web.dto.mapper;

import br.com.threadstech.stockfy.entity.Address;
import br.com.threadstech.stockfy.entity.Contact;
import br.com.threadstech.stockfy.entity.Customer;
import br.com.threadstech.stockfy.validation.AddressCreateDto;
import br.com.threadstech.stockfy.web.dto.ContactCreateDto;
import br.com.threadstech.stockfy.web.dto.CustomerCreateDto;
import br.com.threadstech.stockfy.web.dto.mapper.anotations.IgnoreAuditFields;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface CustomerMapper {

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "payments", ignore = true)
  @IgnoreAuditFields
  Customer toCustomer(CustomerCreateDto customerDto);

  @Mapping(target = "id", ignore = true)
  Contact toContact(ContactCreateDto contactDto);

  @Mapping(target = "id", ignore = true)
  Address toAddress(AddressCreateDto addressDto);
}
