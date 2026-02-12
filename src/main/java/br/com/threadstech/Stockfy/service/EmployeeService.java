package br.com.threadstech.stockfy.service;

import br.com.threadstech.stockfy.components.ConstraintResolver;
import br.com.threadstech.stockfy.config.constraints.EmployeeConstraintNames;
import br.com.threadstech.stockfy.entity.Employee;
import br.com.threadstech.stockfy.enums.PasswordKey;
import br.com.threadstech.stockfy.exception.EmployeeUniqueViolationException;
import br.com.threadstech.stockfy.exception.EntityNotFoundException;
import br.com.threadstech.stockfy.exception.InvalidPasswordException;
import br.com.threadstech.stockfy.repository.EmployeeRepository;
import br.com.threadstech.stockfy.web.dto.EmployeeSummaryDto;
import br.com.threadstech.stockfy.web.dto.EmployeeUpdateDto;
import br.com.threadstech.stockfy.web.dto.PasswordUpdateDto;
import br.com.threadstech.stockfy.web.dto.mapper.EmployeeMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EmployeeService {

  private final EmployeeRepository employeeRepository;
  private final PasswordEncoder passwordEncoder;
  private final ConstraintResolver constraintResolver;

  @Transactional
  public void save(Employee employee) {
    try {
      employee.setPassword(passwordEncoder.encode(employee.getPassword()));
      employeeRepository.save(employee);
    } catch (DataIntegrityViolationException ex) {
      resolveUniqueConstraint(ex);
    }
  }

  @Transactional(readOnly = true)
  public Page<EmployeeSummaryDto> findAll(Pageable pageable, EmployeeMapper employeeMapper) {
    Page<Employee> employees = employeeRepository.findAll(pageable);
    return employeeMapper.toPageSummary(employees);
  }

  @Transactional(readOnly = true)
  public Employee findById(Long id) {
    return employeeRepository
        .findById(id)
        .orElseThrow(() -> new EntityNotFoundException(id.toString()));
  }

  @Transactional(readOnly = true)
  public @Nullable String findCpfById(Long id) {
    return employeeRepository
        .findCpfById(id)
        .orElseThrow(() -> new EntityNotFoundException(id.toString()));
  }

  @Transactional
  public void updateById(
      Long id, EmployeeUpdateDto employeeUpdateDto, EmployeeMapper employeeMapper) {
    try {
      Employee employee = findById(id);
      employeeMapper.update(employeeUpdateDto, employee);
      employeeRepository.save(employee);
    } catch (DataIntegrityViolationException ex) {
      resolveUniqueConstraint(ex);
    }
  }

  @Transactional
  public void updatePasswordById(Long id, @Valid PasswordUpdateDto passwordUpdateDto) {
    String currentPassword =
        employeeRepository
            .findPasswordById(id)
            .orElseThrow(() -> new EntityNotFoundException(id.toString()));

    if (!passwordEncoder.matches(passwordUpdateDto.getCurrentPassword(), currentPassword)) {
      throw new InvalidPasswordException(PasswordKey.CURRENT);
    }
    if (!passwordUpdateDto.getNewPassword().equals(passwordUpdateDto.getConfirmPassword())) {
      throw new InvalidPasswordException(PasswordKey.CONFIRM);
    }
    String newPassword = passwordEncoder.encode(passwordUpdateDto.getNewPassword());
    employeeRepository.updatePasswordById(id, newPassword);
  }

  public void deleteById(Long id) {
    employeeRepository.deleteById(id);
  }

  public Employee findByEmail(String email) {
    return employeeRepository
        .findByEmailWithContact(email)
        .orElseThrow(() -> new EntityNotFoundException(email));
  }

  public long count() {
    return employeeRepository.count();
  }

  private void resolveUniqueConstraint(DataIntegrityViolationException ex)
      throws EmployeeUniqueViolationException {
    String constraint = constraintResolver.resolveDisplayName(ex, EmployeeConstraintNames.class);
    throw new EmployeeUniqueViolationException(constraint);
  }
}
