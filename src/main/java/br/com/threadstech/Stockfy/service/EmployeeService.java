package br.com.threadstech.stockfy.service;

import br.com.threadstech.stockfy.components.ConstraintResolver;
import br.com.threadstech.stockfy.entity.Employee;
import br.com.threadstech.stockfy.enums.PasswordKey;
import br.com.threadstech.stockfy.exception.EntityNotFoundException;
import br.com.threadstech.stockfy.exception.InvalidPasswordException;
import br.com.threadstech.stockfy.repository.EmployeeRepository;
import br.com.threadstech.stockfy.web.dto.EmployeeUpdateDto;
import br.com.threadstech.stockfy.web.dto.PasswordUpdateDto;
import br.com.threadstech.stockfy.web.dto.mapper.EmployeeMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EmployeeService {

  private final EmployeeRepository employeeRepository;
  private final ConstraintResolver constraintResolver;

  @Transactional
  public void save(Employee employee) {
    try {
      // TODO: encrypt password
      employeeRepository.save(employee);
    } catch (DataIntegrityViolationException ex) {
      constraintResolver.resolveConstraint(ex);
    }
  }

  @Transactional(readOnly = true)
  public Page<Employee> findAll(Pageable pageable) {
    return employeeRepository.findAll(pageable);
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
      constraintResolver.resolveConstraint(ex);
    }
  }

  @Transactional
  public void updatePasswordById(Long id, @Valid PasswordUpdateDto passwordUpdateDto) {
    String currentPassword =
        employeeRepository
            .findPasswordById(id)
            .orElseThrow(() -> new EntityNotFoundException(id.toString()));
    if (!passwordUpdateDto.getCurrentPassword().equals(currentPassword)) {
      throw new InvalidPasswordException(PasswordKey.CURRENT);
    }
    if (!passwordUpdateDto.getNewPassword().equals(passwordUpdateDto.getConfirmPassword())) {
      throw new InvalidPasswordException(PasswordKey.CONFIRM);
    }
    // TODO: encrypt password
    employeeRepository.updatePasswordById(id, passwordUpdateDto.getNewPassword());
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
}
