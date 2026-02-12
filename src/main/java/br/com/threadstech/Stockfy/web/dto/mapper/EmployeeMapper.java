package br.com.threadstech.stockfy.web.dto.mapper;

import br.com.threadstech.stockfy.entity.Employee;
import br.com.threadstech.stockfy.entity.EmployeeAddress;
import br.com.threadstech.stockfy.entity.EmployeeContact;
import br.com.threadstech.stockfy.validation.AddressCreateDto;
import br.com.threadstech.stockfy.web.dto.AddressDetailDto;
import br.com.threadstech.stockfy.web.dto.AddressUpdateDto;
import br.com.threadstech.stockfy.web.dto.ContactCreateDto;
import br.com.threadstech.stockfy.web.dto.ContactDetailDto;
import br.com.threadstech.stockfy.web.dto.ContactUpdateDto;
import br.com.threadstech.stockfy.web.dto.EmployeeCreateDto;
import br.com.threadstech.stockfy.web.dto.EmployeeDetailDto;
import br.com.threadstech.stockfy.web.dto.EmployeeSummaryDto;
import br.com.threadstech.stockfy.web.dto.EmployeeUpdateDto;
import br.com.threadstech.stockfy.web.dto.mapper.anotations.IgnoreAddressContactDeleteAudit;
import br.com.threadstech.stockfy.web.dto.mapper.anotations.IgnoreAuditFields;
import br.com.threadstech.stockfy.web.dto.mapper.anotations.IgnoreSoftDeleteFields;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;
import org.springframework.data.domain.Page;

@Mapper
public interface EmployeeMapper {

  @IgnoreAuditFields
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "refreshTokens", ignore = true)
  Employee toEmployee(EmployeeCreateDto employeeCreateDto);

  @IgnoreSoftDeleteFields
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "employees", ignore = true)
  EmployeeContact toContact(ContactCreateDto contactDto);

  @IgnoreSoftDeleteFields
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "employees", ignore = true)
  EmployeeAddress toAddress(AddressCreateDto addressDto);

  @Mapping(source = "contact.email", target = "email")
  @Mapping(source = "contact.phoneNumber", target = "phoneNumber")
  @BeanMapping(unmappedSourcePolicy = ReportingPolicy.IGNORE)
  EmployeeSummaryDto toSummary(Employee customer);

  default Page<EmployeeSummaryDto> toPageSummary(Page<Employee> customerPage) {
    return customerPage.map(this::toSummary);
  }

  @BeanMapping(unmappedSourcePolicy = ReportingPolicy.IGNORE)
  EmployeeDetailDto toDetail(Employee customer);

  @BeanMapping(unmappedSourcePolicy = ReportingPolicy.IGNORE)
  ContactDetailDto toContactDetail(EmployeeContact contact);

  @BeanMapping(unmappedSourcePolicy = ReportingPolicy.IGNORE)
  AddressDetailDto toAddressDetail(EmployeeAddress address);

  @IgnoreAuditFields
  @IgnoreAddressContactDeleteAudit
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "birthday", ignore = true)
  @Mapping(target = "cpf", ignore = true)
  @Mapping(target = "password", ignore = true)
  @Mapping(target = "refreshTokens", ignore = true)
  Employee update(EmployeeUpdateDto customerDto, @MappingTarget Employee customer);

  @BeanMapping(unmappedSourcePolicy = ReportingPolicy.IGNORE)
  ContactUpdateDto toContactUpdate(EmployeeContact contact);

  @BeanMapping(unmappedSourcePolicy = ReportingPolicy.IGNORE)
  AddressUpdateDto toAddressUpdate(EmployeeAddress address);
}
