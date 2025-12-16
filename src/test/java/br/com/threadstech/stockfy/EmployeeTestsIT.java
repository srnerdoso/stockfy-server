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
import br.com.threadstech.stockfy.entity.Employee;
import br.com.threadstech.stockfy.entity.EmployeeAddress;
import br.com.threadstech.stockfy.entity.EmployeeContact;
import br.com.threadstech.stockfy.repository.EmployeeRepository;
import br.com.threadstech.stockfy.utils.EmployeeTestsUtils;
import br.com.threadstech.stockfy.utils.DataGenUtils;
import br.com.threadstech.stockfy.utils.UserUtils;
import br.com.threadstech.stockfy.validation.AddressCreateDto;
import br.com.threadstech.stockfy.web.dto.*;
import br.com.threadstech.stockfy.web.dto.mapper.EmployeeMapper;
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

// FIXME: Revisar e corrigir alguns métodos de teste

@Slf4j
@IntegrationTests
public class EmployeeTestsIT {

  @Autowired private MockMvc mockMvc;
  @Autowired private EmployeeRepository employeeRepository;
  @Autowired private EmployeeMapper employeeMapper;
  @Autowired private EntityManager entityManager;

  @Nested
  @DisplayName("Create Employee")
  class CreateEmployee {

    @BeforeEach
    void setUp() {
      employeeRepository.deleteAll();
    }

    @AdminTest
    void shouldCreateEmployeeWithReturnStatusCreated() throws Exception {
      EmployeeCreateDto employeeDto = EmployeeTestsUtils.validEmployeeCreateDto();

      mockMvc
          .perform(
              post(ApiPaths.EMPLOYEE)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(DataGenUtils.toJson(employeeDto)))
          .andExpect(status().isCreated());

      Employee employee =
          entityManager
              .createQuery(
                  """
                      SELECT c
                      FROM Employee c
                      JOIN FETCH c.contact
                      JOIN FETCH c.address
                      WHERE cpf = :cpf
                      """,
                  Employee.class)
              .setParameter("cpf", employeeDto.getCpf())
              .getSingleResult();

      assertThat(employee).isNotNull();
      assertThat(employee.getCreatedBy()).isEqualTo(UserUtils.admin.name());
      assertThat(employee.getUpdatedBy()).isEqualTo(UserUtils.admin.name());
      assertThat(employee.getCreatedAt()).isNotNull();
      assertThat(employee.getUpdatedAt()).isNotNull();
      assertThat(employee.getCpf()).isEqualTo(employeeDto.getCpf());
      assertThat(employee.getFullName()).isEqualTo(employeeDto.getFullName());
      assertThat(employee.getBirthday()).isEqualTo(employeeDto.getBirthday());

      EmployeeContact contact = employee.getContact();
      ContactCreateDto contactDto = employeeDto.getContact();
      assertThat(contact).isNotNull();
      assertThat(contact.getId()).isNotNull();
      assertThat(contact.getDeletedBy()).isNull();
      assertThat(contact.getDeletedAt()).isNull();
      assertThat(contact.getPhoneNumber()).isEqualTo(contactDto.getPhoneNumber());
      assertThat(contact.getEmail()).isEqualTo(contactDto.getEmail());

      EmployeeAddress address = employee.getAddress();
      AddressCreateDto addressDto = employeeDto.getAddress();
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
    void shouldCreateEmployeeWithReturnStatusBadRequest() throws Exception {
      mockMvc
          .perform(
              post(ApiPaths.EMPLOYEE)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(EmployeeTestsUtils.invalidEmployeeCreateJson()))
          .andExpect(status().isBadRequest());

      mockMvc
          .perform(
              post(ApiPaths.EMPLOYEE)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(EmployeeTestsUtils.nullFieldsEmployeeCreateJson()))
          .andExpect(status().isBadRequest());

      mockMvc
          .perform(
              post(ApiPaths.EMPLOYEE)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(EmployeeTestsUtils.addressContactNullFieldsEmployeeCreateDto()))
          .andExpect(status().isBadRequest());
    }

    @Test
    void shouldCreateEmployeeWithReturnStatusUnauthorized() throws Exception {
      mockMvc.perform(post(ApiPaths.EMPLOYEE)).andExpect(status().isUnauthorized());
    }

    @SalesAttendantTest
    void shouldCreateEmployeeWithSalesAttendantWithReturnStatusForbidden() throws Exception {
      mockMvc
          .perform(
              post(ApiPaths.EMPLOYEE)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(EmployeeTestsUtils.validEmployeeCreateJson()))
          .andDo(print())
          .andExpect(status().isForbidden());
    }

    @InventoryManagerTest
    void shouldCreateEmployeeWithInventoryManagerWithReturnStatusForbidden() throws Exception {
      mockMvc
          .perform(
              post(ApiPaths.EMPLOYEE)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(EmployeeTestsUtils.validEmployeeCreateJson()))
          .andDo(print())
          .andExpect(status().isForbidden());
    }

    @AdminTest
    void shouldCreateEmployeeWithReturnStatusConflict() throws Exception {
      EmployeeCreateDto employeeDto = EmployeeTestsUtils.validEmployeeCreateDto();

      mockMvc.perform(
          post(ApiPaths.EMPLOYEE)
              .contentType(MediaType.APPLICATION_JSON)
              .content(DataGenUtils.toJson(employeeDto)));

      mockMvc
          .perform(
              post(ApiPaths.EMPLOYEE)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(DataGenUtils.toJson(employeeDto)))
          .andExpect(status().isConflict());
    }
  }

  @Nested
  @DisplayName("Find Employee(s)")
  class FindEmployee {

    @AdminTest
    void shouldFindAllEmployeesWithReturnStatusOk() throws Exception {
      employeeRepository.deleteAll();
      int size = 2;

      List<Employee> employees = new ArrayList<>();
      if (employeeRepository.count() == 0) {
        for (int i = 0; i < size; i++) {
          Employee employee = createEmployee();
          employee.setCreatedAt(Instant.now());
          employee.setUpdatedAt(Instant.now());
          employee.setCreatedBy("system");
          employee.setUpdatedBy("system");
          employees.add(employee);
        }
      }
      employeeRepository.saveAll(employees);

      String response =
          mockMvc
              .perform(get(ApiPaths.EMPLOYEE))
              .andExpect(status().isOk())
              .andExpect(content().contentType(MediaType.APPLICATION_JSON))
              .andExpect(jsonPath("$.content").isArray())
              .andExpect(jsonPath("$.content.length()").value(size))
              .andReturn()
              .getResponse()
              .getContentAsString();

      List<Map<String, Object>> employeesFromJson = JsonPath.read(response, "$.content");
      employeesFromJson.forEach(EmployeeTestsIT.this::validateEmployeeSummaryResponse);
    }

    @Test
    void shouldFindAllEmployeesWithReturnStatusUnauthorized() throws Exception {
      mockMvc.perform(get(ApiPaths.EMPLOYEE)).andExpect(status().isUnauthorized());
    }

    @InventoryManagerTest
    void shouldFindAllEmployeesWithInventoryManagerWithReturnStatusForbidden() throws Exception {
      mockMvc.perform(get(ApiPaths.EMPLOYEE)).andExpect(status().isForbidden());
    }

    @SalesAttendantTest
    void shouldFindAllEmployeesWithSalesAttendantWithReturnStatusForbidden() throws Exception {
      mockMvc.perform(get(ApiPaths.EMPLOYEE)).andExpect(status().isForbidden());
    }

    @AdminTest
    void shouldFindByIdWithReturnStatusOk() throws Exception {
      Employee employee = createEmployee();
      employeeRepository.save(employee);

      String response =
          mockMvc
              .perform(get(patternPath(employee.getId())))
              .andExpect(status().isOk())
              .andExpect(content().contentType(MediaType.APPLICATION_JSON))
              .andReturn()
              .getResponse()
              .getContentAsString();

      Map<String, Object> employeeFromJson = JsonPath.read(response, "$");
      validateEmployeeDetailResponse(employeeFromJson);
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
      Employee employee = createEmployee();
      employeeRepository.save(employee);

      String response =
          mockMvc
              .perform(get(patternPath(employee.getId()) + "/cpf"))
              .andExpect(status().isOk())
              .andExpect(content().contentType("text/plain;charset=ISO-8859-1"))
              .andReturn()
              .getResponse()
              .getContentAsString();

      assertThat(response).isEqualTo(employee.getCpf());
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
  @DisplayName("Update Employee")
  class UpdateEmployee {

    @AdminTest
    void shouldUpdateEmployeeWithReturnStatusNoContent() throws Exception {
      Employee employee = employeeRepository.save(createEmployee());
      EmployeeUpdateDto employeeDto = EmployeeTestsUtils.validEmployeeUpdateDto();

      long employeeId = employee.getId();
      mockMvc
          .perform(
              patch(patternPath(employeeId))
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(DataGenUtils.toJson(employeeDto)))
          .andExpect(status().isNoContent());

      Employee employeeUpdated =
          entityManager
              .createQuery(
                  """
              SELECT c
              FROM Employee c
              LEFT JOIN FETCH c.contact
              LEFT JOIN FETCH c.address
              WHERE c.id = :id
              """,
                  Employee.class)
              .setParameter("id", employeeId)
              .getSingleResult();

      assertThat(employeeUpdated).isNotNull();
      assertThat(employeeUpdated.getCreatedBy()).isEqualTo(UserUtils.admin.name());
      assertThat(employeeUpdated.getUpdatedBy()).isEqualTo(UserUtils.admin.name());
      assertThat(employeeUpdated.getCreatedAt()).isNotNull();
      assertThat(employeeUpdated.getUpdatedAt()).isNotNull();
      assertThat(employeeUpdated.getFullName()).isEqualTo(employeeDto.getFullName());

      EmployeeContact contact = employeeUpdated.getContact();
      ContactUpdateDto contactDto = employeeDto.getContact();
      assertThat(contact).isNotNull();
      assertThat(contact.getId()).isNotNull();
      assertThat(contact.getDeletedBy()).isNull();
      assertThat(contact.getDeletedAt()).isNull();
      assertThat(contact.getPhoneNumber())
          .isEqualTo(Objects.requireNonNull(contactDto).getPhoneNumber());
      assertThat(contact.getEmail()).isEqualTo(contactDto.getEmail());

      EmployeeAddress address = employeeUpdated.getAddress();
      AddressUpdateDto addressDto = employeeDto.getAddress();
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
    void shouldUpdateEmployeeWithReturnStatusBadRequest() throws Exception {
      Employee employee = employeeRepository.save(createEmployee());
      mockMvc
          .perform(
              patch(patternPath(employee.getId()))
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(EmployeeTestsUtils.invalidEmployeeUpdateJson()))
          .andExpect(status().isBadRequest());
    }

    @AdminTest
    void shouldUpdateEmployeeWithReturnStatusNotFound() throws Exception {
      mockMvc
          .perform(
              patch(patternPath(418941985198L))
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(EmployeeTestsUtils.validEmployeeUpdateJson()))
          .andExpect(status().isNotFound());
    }

    @Test
    void shouldUpdateEmployeeWithReturnStatusUnauthorized() throws Exception {
      mockMvc.perform(patch(patternPath(372108937120L))).andExpect(status().isUnauthorized());
    }

    @InventoryManagerTest
    void shouldUpdateEmployeeWithInventoryManagerWithReturnStatusForbidden() throws Exception {
      mockMvc.perform(patch(patternPath(1L))).andExpect(status().isForbidden());
    }

    @SalesAttendantTest
    void shouldUpdateEmployeeWithSalesAttendantWithReturnStatusForbidden() throws Exception {
      mockMvc.perform(patch(patternPath(1L))).andExpect(status().isForbidden());
    }
  }

  @Nested
  @DisplayName("Delete Employee")
  class DeleteEmployee {

    @AdminTest
    void shouldDeleteEmployeeWithReturnStatusNoContent() throws Exception {
      Employee employee = employeeRepository.save(createEmployee());
      mockMvc.perform(delete(patternPath(employee.getId()))).andExpect(status().isNoContent());
      Employee employeeDeleted = employeeRepository.findById(employee.getId()).orElse(null);
      assertThat(employeeDeleted).isNull();
    }

    @Test
    void shouldDeleteEmployeeWithReturnStatusUnauthorized() throws Exception {
      mockMvc.perform(delete(patternPath(1L))).andExpect(status().isUnauthorized());
    }

    @InventoryManagerTest
    void shouldDeleteEmployeeWithInventoryManagerWithReturnStatusForbidden() throws Exception {
      mockMvc.perform(delete(patternPath(1L))).andExpect(status().isForbidden());
    }

    @SalesAttendantTest
    void shouldDeleteEmployeeWithSalesAttendantWithReturnStatusForbidden() throws Exception {
      mockMvc.perform(delete(patternPath(1L))).andExpect(status().isForbidden());
    }
  }

  private Employee createEmployee() {
    EmployeeCreateDto employeeDto = EmployeeTestsUtils.validEmployeeCreateDto();
    return employeeMapper.toEmployee(employeeDto);
  }

  private void validateEmployeeSummaryResponse(Map<String, Object> response) {
    Field[] dtoFields = EmployeeSummaryDto.class.getDeclaredFields();
    List<String> expectedFieldNames = Arrays.stream(dtoFields).map(Field::getName).toList();
    expectedFieldsLog(expectedFieldNames);

    expectedFieldNames.forEach(
        fieldName -> {
          employeeContainsLog(response.get("cpf"), fieldName, response.containsKey(fieldName));
          assertThat(response)
              .as("Employee should have field '%s' defined in response.", fieldName)
              .containsKey(fieldName);
        });
  }

  private void validateEmployeeDetailResponse(Map<String, Object> response) {
    Field[] dtoFields = EmployeeDetailDto.class.getDeclaredFields();
    List<String> expectedFieldNames = Arrays.stream(dtoFields).map(Field::getName).toList();
    expectedFieldsLog(expectedFieldNames);

    expectedFieldNames.forEach(
        fieldName -> {
          employeeContainsLog(response.get("fullName"), fieldName, response.containsKey(fieldName));

          assertThat(response)
              .as("Employee should have field '%s' defined in response.", fieldName)
              .containsKey(fieldName);
        });
  }

  private String patternPath(String path, String resource) {
    return ApiPaths.EMPLOYEE + "/" + path + "/" + resource;
  }

  private String patternPath(String path) {
    return ApiPaths.EMPLOYEE + "/" + path;
  }

  private String patternPath(Long path) {
    return patternPath(path.toString());
  }

  private void employeeContainsLog(
      Object responseObjectFullName, String fieldName, boolean isContainsKey) {
    log.info(
        "Employee '{}' contains field '{}': {}", responseObjectFullName, fieldName, isContainsKey);
  }

  private void expectedFieldsLog(List<String> fieldNames) {
    log.info("Expected field names: {}", fieldNames);
  }
}
