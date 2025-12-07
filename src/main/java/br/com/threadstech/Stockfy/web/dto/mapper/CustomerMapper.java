package br.com.threadstech.stockfy.web.dto.mapper;

import br.com.threadstech.stockfy.entity.Customer;
import br.com.threadstech.stockfy.entity.CustomerAddress;
import br.com.threadstech.stockfy.entity.CustomerContact;
import br.com.threadstech.stockfy.validation.AddressCreateDto;
import br.com.threadstech.stockfy.web.dto.AddressUpdateDto;
import br.com.threadstech.stockfy.web.dto.ContactCreateDto;
import br.com.threadstech.stockfy.web.dto.ContactUpdateDto;
import br.com.threadstech.stockfy.web.dto.CustomerAddressDetailDto;
import br.com.threadstech.stockfy.web.dto.CustomerContactDetailDto;
import br.com.threadstech.stockfy.web.dto.CustomerCreateDto;
import br.com.threadstech.stockfy.web.dto.CustomerDetailDto;
import br.com.threadstech.stockfy.web.dto.CustomerSummaryDto;
import br.com.threadstech.stockfy.web.dto.CustomerUpdateDto;
import br.com.threadstech.stockfy.web.dto.mapper.anotations.IgnoreAuditFields;
import br.com.threadstech.stockfy.web.dto.mapper.anotations.IgnoreCustomerSoftDeleteFields;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;
import org.springframework.data.domain.Page;

@Mapper
public interface CustomerMapper {

  @IgnoreAuditFields
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "payments", ignore = true)
  @Mapping(target = "deleted", ignore = true)
  Customer toCustomer(CustomerCreateDto customerDto);

  @IgnoreCustomerSoftDeleteFields
  @Mapping(target = "id", ignore = true)
  CustomerContact toContact(ContactCreateDto contactDto);

  @IgnoreCustomerSoftDeleteFields
  @Mapping(target = "id", ignore = true)
  CustomerAddress toAddress(AddressCreateDto addressDto);

  @Mapping(source = "contact.email", target = "email")
  @Mapping(source = "contact.phoneNumber", target = "phoneNumber")
  @BeanMapping(unmappedSourcePolicy = ReportingPolicy.IGNORE)
  CustomerSummaryDto toSummary(Customer customer);

  default Page<CustomerSummaryDto> toPageSummary(Page<Customer> customerPage) {
    return customerPage.map(this::toSummary);
  }

  @BeanMapping(unmappedSourcePolicy = ReportingPolicy.IGNORE)
  CustomerDetailDto toDetail(Customer customer);

  @BeanMapping(unmappedSourcePolicy = ReportingPolicy.IGNORE)
  CustomerContactDetailDto toContactDetail(CustomerContact contact);

  @BeanMapping(unmappedSourcePolicy = ReportingPolicy.IGNORE)
  CustomerAddressDetailDto toAddressDetail(CustomerAddress address);

  @IgnoreAuditFields
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "birthday", ignore = true)
  @Mapping(target = "cpf", ignore = true)
  @Mapping(target = "deleted", ignore = true)
  @Mapping(target = "payments", ignore = true)
  @Mapping(target = "address.deletedBy", ignore = true)
  @Mapping(target = "address.deletedAt", ignore = true)
  @Mapping(target = "contact.deletedBy", ignore = true)
  @Mapping(target = "contact.deletedAt", ignore = true)
  Customer update(CustomerUpdateDto customerDto, @MappingTarget Customer customer);

  @BeanMapping(unmappedSourcePolicy = ReportingPolicy.IGNORE)
  ContactUpdateDto toContactUpdate(CustomerContact contact);

  @BeanMapping(unmappedSourcePolicy = ReportingPolicy.IGNORE)
  AddressUpdateDto toAddressUpdate(CustomerAddress address);
}
