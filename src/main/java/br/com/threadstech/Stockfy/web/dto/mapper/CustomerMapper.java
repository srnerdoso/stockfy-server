package br.com.threadstech.stockfy.web.dto.mapper;

import br.com.threadstech.stockfy.entity.Customer;
import br.com.threadstech.stockfy.entity.CustomerAddress;
import br.com.threadstech.stockfy.entity.CustomerContact;
import br.com.threadstech.stockfy.validation.AddressCreateDto;
import br.com.threadstech.stockfy.web.dto.*;
import br.com.threadstech.stockfy.web.dto.mapper.anotations.IgnoreAuditFields;
import br.com.threadstech.stockfy.web.dto.mapper.anotations.IgnoreCustomerSoftDeleteFields;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
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
}
