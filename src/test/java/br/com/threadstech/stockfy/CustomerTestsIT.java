package br.com.threadstech.stockfy;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import br.com.threadstech.stockfy.annotations.AdminTest;
import br.com.threadstech.stockfy.annotations.IntegrationTests;
import br.com.threadstech.stockfy.annotations.InventoryManagerTest;
import br.com.threadstech.stockfy.annotations.SalesAttendantTest;
import br.com.threadstech.stockfy.api.ApiPaths;
import br.com.threadstech.stockfy.entity.Customer;
import br.com.threadstech.stockfy.entity.CustomerAddress;
import br.com.threadstech.stockfy.entity.CustomerContact;
import br.com.threadstech.stockfy.repository.CustomerRepository;
import br.com.threadstech.stockfy.utils.CustomerTestsUtils;
import br.com.threadstech.stockfy.utils.DataGenUtils;
import br.com.threadstech.stockfy.utils.UserUtils;
import br.com.threadstech.stockfy.validation.AddressCreateDto;
import br.com.threadstech.stockfy.web.dto.*;
import br.com.threadstech.stockfy.web.dto.mapper.CustomerMapper;
import com.jayway.jsonpath.JsonPath;
import jakarta.persistence.EntityManager;
import java.lang.reflect.Field;
import java.time.Instant;
import java.util.*;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@Slf4j
@IntegrationTests
public class CustomerTestsIT {

  @Autowired private MockMvc mockMvc;
  @Autowired private CustomerRepository customerRepository;
  @Autowired private CustomerMapper customerMapper;
  @Autowired private EntityManager entityManager;

  @Nested
  @DisplayName("Create Customer")
  class CreateCustomer {

    @BeforeEach
    void setUp() {
      customerRepository.deleteAll();
    }

    @AdminTest
    void shouldCreateCustomerWithReturnStatusCreated() throws Exception {
      CustomerCreateDto customerDto = CustomerTestsUtils.validCustomerCreateDto();

      mockMvc
          .perform(
              post(ApiPaths.CUSTOMER)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(DataGenUtils.toJson(customerDto)))
          .andExpect(status().isCreated());

      Customer customer =
          entityManager
              .createQuery(
                  """
                      SELECT c
                      FROM Customer c
                      JOIN FETCH c.contact
                      JOIN FETCH c.address
                      WHERE cpf = :cpf
                      """,
                  Customer.class)
              .setParameter("cpf", customerDto.getCpf())
              .getSingleResult();

      assertThat(customer).isNotNull();
      assertThat(customer.getCreatedBy()).isEqualTo(UserUtils.admin.name());
      assertThat(customer.getUpdatedBy()).isEqualTo(UserUtils.admin.name());
      assertThat(customer.getCreatedAt()).isNotNull();
      assertThat(customer.getUpdatedAt()).isNotNull();
      assertThat(customer.getCpf()).isEqualTo(customerDto.getCpf());
      assertThat(customer.getFullName()).isEqualTo(customerDto.getFullName());
      assertThat(customer.getBirthday()).isEqualTo(customerDto.getBirthday());

      CustomerContact contact = customer.getContact();
      ContactCreateDto contactDto = customerDto.getContact();
      assertThat(contact).isNotNull();
      assertThat(contact.getId()).isNotNull();
      assertThat(contact.getDeletedBy()).isNull();
      assertThat(contact.getDeletedAt()).isNull();
      assertThat(contact.getPhoneNumber()).isEqualTo(contactDto.getPhoneNumber());
      assertThat(contact.getEmail()).isEqualTo(contactDto.getEmail());

      CustomerAddress address = customer.getAddress();
      AddressCreateDto addressDto = customerDto.getAddress();
      assertThat(address).isNotNull();
      assertThat(address.getId()).isNotNull();
      assertThat(address.getDeletedBy()).isNull();
      assertThat(address.getDeletedAt()).isNull();
      assertThat(address.getCity()).isEqualTo(addressDto.getCity());
      assertThat(address.getStreet()).isEqualTo(addressDto.getStreet());
      assertThat(address.getNumber()).isEqualTo(addressDto.getNumber());
      assertThat(address.getComplement()).isEqualTo(addressDto.getComplement());
      assertThat(address.getNeighborhood()).isEqualTo(addressDto.getNeighborhood());
      assertThat(address.getState()).isEqualTo(addressDto.getState());
      assertThat(address.getZipCode()).isEqualTo(addressDto.getZipCode());
      assertThat(address.getCountry()).isEqualTo(addressDto.getCountry());
    }

    @AdminTest
    void shouldCreateCustomerWithReturnStatusBadRequest() throws Exception {
      mockMvc
          .perform(
              post(ApiPaths.CUSTOMER)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(CustomerTestsUtils.invalidCustomerCreateJson()))
          .andExpect(status().isBadRequest());

      mockMvc
          .perform(
              post(ApiPaths.CUSTOMER)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(CustomerTestsUtils.nullFieldsCustomerCreateJson()))
          .andExpect(status().isBadRequest());

      mockMvc
          .perform(
              post(ApiPaths.CUSTOMER)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(CustomerTestsUtils.addressContactNullFieldsCustomerCreateDto()))
          .andExpect(status().isBadRequest());
    }

    @Test
    void shouldCreateCustomerWithReturnStatusUnauthorized() throws Exception {
      mockMvc.perform(post(ApiPaths.CUSTOMER)).andExpect(status().isUnauthorized());
    }

    @SalesAttendantTest
    void shouldCreateCustomerWithSalesAttendantWithReturnStatusForbidden() throws Exception {
      mockMvc
          .perform(
              post(ApiPaths.CUSTOMER)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(CustomerTestsUtils.validCustomerCreateJson()))
          .andDo(print())
          .andExpect(status().isForbidden());
    }

    @InventoryManagerTest
    void shouldCreateCustomerWithInventoryManagerWithReturnStatusForbidden() throws Exception {
      mockMvc
          .perform(
              post(ApiPaths.CUSTOMER)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(CustomerTestsUtils.validCustomerCreateJson()))
          .andDo(print())
          .andExpect(status().isForbidden());
    }

    @AdminTest
    void shouldCreateCustomerWithReturnStatusConflict() throws Exception {
      CustomerCreateDto customerDto = CustomerTestsUtils.validCustomerCreateDto();

      mockMvc.perform(
          post(ApiPaths.CUSTOMER)
              .contentType(MediaType.APPLICATION_JSON)
              .content(DataGenUtils.toJson(customerDto)));

      mockMvc
          .perform(
              post(ApiPaths.CUSTOMER)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(DataGenUtils.toJson(customerDto)))
          .andExpect(status().isConflict());
    }
  }

  @Nested
  @DisplayName("Find Customer(s)")
  class FindCustomer {

    @AdminTest
    void shouldFindAllCustomersWithReturnStatusOk() throws Exception {
      customerRepository.deleteAll();
      int size = 2;

      List<Customer> customers = new ArrayList<>();
      if (customerRepository.count() == 0) {
        for (int i = 0; i < size; i++) {
          Customer customer = createCustomer();
          customer.setCreatedAt(Instant.now());
          customer.setUpdatedAt(Instant.now());
          customer.setCreatedBy("system");
          customer.setUpdatedBy("system");
          customers.add(customer);
        }
      }
      customerRepository.saveAll(customers);

      String response =
          mockMvc
              .perform(get(ApiPaths.CUSTOMER))
              .andExpect(status().isOk())
              .andExpect(content().contentType(MediaType.APPLICATION_JSON))
              .andExpect(jsonPath("$.content").isArray())
              .andExpect(jsonPath("$.content.length()").value(size))
              .andReturn()
              .getResponse()
              .getContentAsString();

      List<Map<String, Object>> customersFromJson = JsonPath.read(response, "$.content");
      customersFromJson.forEach(CustomerTestsIT.this::validateCustomerSummaryResponse);
    }

    @Test
    void shouldFindAllCustomersWithReturnStatusUnauthorized() throws Exception {
      mockMvc.perform(get(ApiPaths.CUSTOMER)).andExpect(status().isUnauthorized());
    }

    @InventoryManagerTest
    void shouldFindAllCustomersWithInventoryManagerWithReturnStatusForbidden() throws Exception {
      mockMvc.perform(get(ApiPaths.CUSTOMER)).andExpect(status().isForbidden());
    }

    @SalesAttendantTest
    void shouldFindAllCustomersWithSalesAttendantWithReturnStatusForbidden() throws Exception {
      mockMvc.perform(get(ApiPaths.CUSTOMER)).andExpect(status().isForbidden());
    }

    @AdminTest
    void shouldFindByIdWithReturnStatusOk() throws Exception {
      Customer customer = createCustomer();
      customerRepository.save(customer);

      String response =
          mockMvc
              .perform(get(patternPath(customer.getId())))
              .andExpect(status().isOk())
              .andExpect(content().contentType(MediaType.APPLICATION_JSON))
              .andReturn()
              .getResponse()
              .getContentAsString();

      Map<String, Object> customerFromJson = JsonPath.read(response, "$");
      validateCustomerDetailResponse(customerFromJson);
    }

    @AdminTest
    void shouldFindByIdWithReturnStatusNotFound() throws Exception {
      mockMvc.perform(get(patternPath(1561968541651891L))).andExpect(status().isNotFound());
    }

    @Test
    void shouldFindByIdWithReturnStatusUnauthorized() throws Exception {
      mockMvc.perform(get(patternPath("1"))).andExpect(status().isUnauthorized());
    }

    @InventoryManagerTest
    void shouldFindByIdWithInventoryManagerWithReturnStatusForbidden() throws Exception {
      mockMvc.perform(get(patternPath("1"))).andExpect(status().isForbidden());
    }

    @SalesAttendantTest
    void shouldFindByIdWithSalesAttendantWithReturnStatusForbidden() throws Exception {
      mockMvc.perform(get(patternPath("1"))).andExpect(status().isForbidden());
    }

    @AdminTest
    void shouldFindByCpfWithReturnStatusOk() throws Exception {
      Customer customer = createCustomer();
      customerRepository.save(customer);

      String response =
          mockMvc
              .perform(get(patternPath(customer.getId()) + "/cpf"))
              .andExpect(status().isOk())
              .andExpect(content().contentType("text/plain;charset=ISO-8859-1"))
              .andReturn()
              .getResponse()
              .getContentAsString();

      assertThat(response).isEqualTo(customer.getCpf());
    }

    @AdminTest
    void shouldFindByCpfWithReturnStatusNotFound() throws Exception {
      mockMvc.perform(get(patternPath(1596189196854L) + "/cpf")).andExpect(status().isNotFound());
    }

    @Test
    void shouldFindByCpfWithReturnStatusUnauthorized() throws Exception {
      mockMvc.perform(get(patternPath(1L) + "/cpf")).andExpect(status().isUnauthorized());
    }

    @InventoryManagerTest
    void shouldFindByCpfWithInventoryManagerWithReturnStatusForbidden() throws Exception {
      mockMvc.perform(get(patternPath(1L) + "/cpf")).andExpect(status().isForbidden());
    }

    @SalesAttendantTest
    void shouldFindByCpfWithSalesAttendantWithReturnStatusForbidden() throws Exception {
      mockMvc.perform(get(patternPath(1L) + "/cpf")).andExpect(status().isForbidden());
    }
  }

  @Nested
  @DisplayName("Update Customer")
  class UpdateCustomer {

    @AdminTest
    void shouldUpdateCustomerWithReturnStatusNoContent() throws Exception {
      Customer customer = customerRepository.save(createCustomer());
      CustomerUpdateDto customerDto = CustomerTestsUtils.validCustomerUpdateDto();

      long customerId = customer.getId();
      mockMvc
          .perform(
              patch(patternPath(customerId))
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(DataGenUtils.toJson(customerDto)))
          .andExpect(status().isNoContent());

      Customer customerUpdated =
          entityManager
              .createQuery(
                  """
              SELECT c
              FROM Customer c
              LEFT JOIN FETCH c.contact
              LEFT JOIN FETCH c.address
              WHERE c.id = :id
              """,
                  Customer.class)
              .setParameter("id", customerId)
              .getSingleResult();

      assertThat(customerUpdated).isNotNull();
      assertThat(customerUpdated.getCreatedBy()).isEqualTo(UserUtils.admin.name());
      assertThat(customerUpdated.getUpdatedBy()).isEqualTo(UserUtils.admin.name());
      assertThat(customerUpdated.getCreatedAt()).isNotNull();
      assertThat(customerUpdated.getUpdatedAt()).isNotNull();
      assertThat(customerUpdated.getFullName()).isEqualTo(customerDto.getFullName());

      CustomerContact contact = customerUpdated.getContact();
      ContactUpdateDto contactDto = customerDto.getContact();
      assertThat(contact).isNotNull();
      assertThat(contact.getId()).isNotNull();
      assertThat(contact.getDeletedBy()).isNull();
      assertThat(contact.getDeletedAt()).isNull();
      assertThat(contact.getPhoneNumber())
          .isEqualTo(Objects.requireNonNull(contactDto).getPhoneNumber());
      assertThat(contact.getEmail()).isEqualTo(contactDto.getEmail());

      CustomerAddress address = customerUpdated.getAddress();
      AddressUpdateDto addressDto = customerDto.getAddress();
      assertThat(address).isNotNull();
      assertThat(address.getId()).isNotNull();
      assertThat(address.getDeletedBy()).isNull();
      assertThat(address.getDeletedAt()).isNull();
      assertThat(address.getCity()).isEqualTo(Objects.requireNonNull(addressDto).getCity());
      assertThat(address.getStreet()).isEqualTo(addressDto.getStreet());
      assertThat(address.getNumber()).isEqualTo(addressDto.getNumber());
      assertThat(address.getComplement()).isEqualTo(addressDto.getComplement());
      assertThat(address.getNeighborhood()).isEqualTo(addressDto.getNeighborhood());
      assertThat(address.getState()).isEqualTo(addressDto.getState());
      assertThat(address.getZipCode()).isEqualTo(addressDto.getZipCode());
      assertThat(address.getCountry()).isEqualTo(addressDto.getCountry());
    }

    @AdminTest
    void shouldUpdateCustomerWithReturnStatusBadRequest() throws Exception {
      Customer customer = customerRepository.save(createCustomer());
      mockMvc
          .perform(
              patch(patternPath(customer.getId()))
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(CustomerTestsUtils.invalidCustomerUpdateJson()))
          .andExpect(status().isBadRequest());
    }

    @AdminTest
    void shouldUpdateCustomerWithReturnStatusNotFound() throws Exception {
      mockMvc
          .perform(
              patch(patternPath(418941985198L))
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(CustomerTestsUtils.validCustomerUpdateJson()))
          .andExpect(status().isNotFound());
    }

    @Test
    void shouldUpdateCustomerWithReturnStatusUnauthorized() throws Exception {
      mockMvc.perform(patch(patternPath(372108937120L))).andExpect(status().isUnauthorized());
    }

    @InventoryManagerTest
    void shouldUpdateCustomerWithInventoryManagerWithReturnStatusForbidden() throws Exception {
      mockMvc.perform(patch(patternPath(1L))).andExpect(status().isForbidden());
    }

    @SalesAttendantTest
    void shouldUpdateCustomerWithSalesAttendantWithReturnStatusForbidden() throws Exception {
      mockMvc.perform(patch(patternPath(1L))).andExpect(status().isForbidden());
    }
  }

  @Nested
  @DisplayName("Delete Customer")
  class DeleteCustomer {

    @AdminTest
    void shouldDeleteCustomerWithReturnStatusNoContent() throws Exception {
      Customer customer = customerRepository.save(createCustomer());
      mockMvc.perform(delete(patternPath(customer.getId()))).andExpect(status().isNoContent());

      String deleted = "deleted";

      Customer customerDeleted =
          (Customer)
              entityManager
                  .createNativeQuery(
                      """
                    SELECT c.*
                    FROM customers c
                    JOIN customer_contacts cc
                        ON cc.id = c.customer_contact_id
                    JOIN customer_addresses ca
                        ON ca.id = c.customer_address_id
                    WHERE c.id = :id
                    """,
                      Customer.class)
                  .setParameter("id", customer.getId())
                  .getSingleResult();

      assertThat(customerDeleted).isNotNull();
      assertThat(customerDeleted.isDeleted()).isTrue();
      assertThat(customerDeleted.getFullName()).isEqualTo("deleted");
      assertThat(customerDeleted.getCpf()).endsWith("cpf_" + customer.getId() + "_deleted");

      CustomerContact contact = entityManager.find(CustomerContact.class, customer.getContact().getId());
      assertThat(contact).isNotNull();
      assertThat(contact.isDeleted()).isTrue();
      assertThat(contact.getEmail()).endsWith("email_" + customer.getId() + "_deleted");
      assertThat(contact.getPhoneNumber())
          .endsWith("phone_number_" + customer.getId() + "_deleted");

      CustomerAddress address = entityManager.find(CustomerAddress.class, customer.getAddress().getId());
      assertThat(address).isNotNull();
      assertThat(address.isDeleted()).isTrue();
      assertThat(address.getStreet()).isEqualTo(deleted);
      assertThat(address.getNumber()).isEqualTo(deleted);
      assertThat(address.getComplement()).isEqualTo(deleted);
      assertThat(address.getNeighborhood()).isEqualTo(deleted);
      assertThat(address.getCity()).isEqualTo(deleted);
      assertThat(address.getState()).isEqualTo(deleted);
      assertThat(address.getZipCode()).isEqualTo(deleted);
      assertThat(address.getCountry()).isEqualTo(deleted);
    }

    @Test
    void shouldDeleteCustomerWithReturnStatusUnauthorized() throws Exception {
      mockMvc.perform(delete(patternPath(1L))).andExpect(status().isUnauthorized());
    }

    @InventoryManagerTest
    void shouldDeleteCustomerWithInventoryManagerWithReturnStatusForbidden() throws Exception {
      mockMvc.perform(delete(patternPath(1L))).andExpect(status().isForbidden());
    }

    @SalesAttendantTest
    void shouldDeleteCustomerWithSalesAttendantWithReturnStatusForbidden() throws Exception {
      mockMvc.perform(delete(patternPath(1L))).andExpect(status().isForbidden());
    }
  }

  private Customer createCustomer() {
    CustomerCreateDto customerDto = CustomerTestsUtils.validCustomerCreateDto();
    return customerMapper.toCustomer(customerDto);
  }

  private void validateCustomerSummaryResponse(Map<String, Object> response) {
    Field[] dtoFields = CustomerSummaryDto.class.getDeclaredFields();
    List<String> expectedFieldNames = Arrays.stream(dtoFields).map(Field::getName).toList();
    expectedFieldsLog(expectedFieldNames);

    expectedFieldNames.forEach(
        fieldName -> {
          customerContainsLog(response.get("cpf"), fieldName, response.containsKey(fieldName));
          assertThat(response)
              .as("Customer should have field '%s' defined in response.", fieldName)
              .containsKey(fieldName);
        });
  }

  private void validateCustomerDetailResponse(Map<String, Object> response) {
    Field[] dtoFields = CustomerDetailDto.class.getDeclaredFields();
    List<String> expectedFieldNames = Arrays.stream(dtoFields).map(Field::getName).toList();
    expectedFieldsLog(expectedFieldNames);

    expectedFieldNames.forEach(
        fieldName -> {
          customerContainsLog(response.get("fullName"), fieldName, response.containsKey(fieldName));

          assertThat(response)
              .as("Customer should have field '%s' defined in response.", fieldName)
              .containsKey(fieldName);
        });
  }

  private String patternPath(String path, String resource) {
    return ApiPaths.CUSTOMER + "/" + path + "/" + resource;
  }

  private String patternPath(String path) {
    return ApiPaths.CUSTOMER + "/" + path;
  }

  private String patternPath(Long path) {
    return patternPath(path.toString());
  }

  private void customerContainsLog(
      Object responseObjectFullName, String fieldName, boolean isContainsKey) {
    log.info(
        "Customer '{}' contains field '{}': {}", responseObjectFullName, fieldName, isContainsKey);
  }

  private void expectedFieldsLog(List<String> fieldNames) {
    log.info("Expected field names: {}", fieldNames);
  }
}
