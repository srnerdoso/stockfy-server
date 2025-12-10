package br.com.threadstech.stockfy.config;

import br.com.threadstech.stockfy.config.properties.AdminBootStrapProps;
import br.com.threadstech.stockfy.config.properties.AdminBootstrapAddressProps;
import br.com.threadstech.stockfy.config.properties.AdminBootstrapContactProps;
import br.com.threadstech.stockfy.entity.Employee;
import br.com.threadstech.stockfy.entity.EmployeeAddress;
import br.com.threadstech.stockfy.entity.EmployeeContact;
import br.com.threadstech.stockfy.enums.Role;
import br.com.threadstech.stockfy.service.EmployeeService;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Slf4j
@Configuration
@RequiredArgsConstructor
@ConfigurationProperties(prefix = "owner")
@Profile("!test")
public class AdminBootstrapConfig implements CommandLineRunner {

  private final EmployeeService employeeService;
  private final AdminBootStrapProps props;
  private final AdminBootstrapAddressProps address;
  private final AdminBootstrapContactProps contact;

  @Override
  public void run(String... args) throws Exception {
    if (employeeService.count() > 0) {
      return;
    }

    log.info("Creating admin user...");
    Employee employee = new Employee();
    employee.setFullName(props.getFullName());
    employee.setCpf(props.getCpf());
    employee.setBirthday(LocalDate.parse(props.getBirthday()));
    employee.setPassword(props.getPassword());
    employee.setRole(Role.ADMIN);

    EmployeeContact employeeContact = new EmployeeContact();
    employeeContact.setEmail(contact.getEmail());
    employeeContact.setPhoneNumber(contact.getPhoneNumber());

    employee.setContact(employeeContact);

    EmployeeAddress employeeAddress = new EmployeeAddress();
    employeeAddress.setStreet(address.getStreet());
    employeeAddress.setNumber(address.getNumber());
    employeeAddress.setComplement(address.getComplement());
    employeeAddress.setNeighborhood(address.getNeighborhood());
    employeeAddress.setCity(address.getCity());
    employeeAddress.setState(address.getState());
    employeeAddress.setZipCode(address.getZipCode());
    employeeAddress.setCountry(address.getCountry());

    employee.setAddress(employeeAddress);

    employeeService.save(employee);
    log.info("Admin user created successfully.");
  }
}
