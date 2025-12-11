package br.com.threadstech.stockfy.service;

import br.com.threadstech.stockfy.components.ConstraintResolver;
import br.com.threadstech.stockfy.config.constraints.CustomerConstraintNames;
import br.com.threadstech.stockfy.entity.Customer;
import br.com.threadstech.stockfy.exception.CustomerUniqueViolationException;
import br.com.threadstech.stockfy.exception.EntityNotFoundException;
import br.com.threadstech.stockfy.repository.CustomerRepository;
import br.com.threadstech.stockfy.web.dto.CustomerUpdateDto;
import br.com.threadstech.stockfy.web.dto.mapper.CustomerMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerService {

  private final CustomerRepository customerRepository;
  private final ConstraintResolver constraintResolver;

  @Transactional
  public void save(Customer customer) {
    try {
      Customer customerSaved = customerRepository.save(customer);
      log.info("Customer saved successfully with id={}", customerSaved.getId());
    } catch (DataIntegrityViolationException ex) {
      resolveUniqueConstraint(ex);
    }
  }

  @Transactional(readOnly = true)
  public Customer findById(Long id) {
    return customerRepository
        .findById(id)
        .orElseThrow(() -> new EntityNotFoundException(id.toString()));
  }

  @Transactional(readOnly = true)
  public Page<Customer> findAll(Pageable pageable) {
    return customerRepository.findAll(pageable);
  }

  @Transactional(readOnly = true)
  public String findCpfById(Long id) {
    return customerRepository
        .findCpfById(id)
        .orElseThrow(() -> new EntityNotFoundException(id.toString()));
  }

  public void updateById(Long id, CustomerUpdateDto customerDto, CustomerMapper customerMapper) {
    try {
      Customer customer = findById(id);
      Customer updatedCustomer = customerMapper.update(customerDto, customer);
      customerRepository.save(updatedCustomer);
    } catch (DataIntegrityViolationException ex) {
      resolveUniqueConstraint(ex);
    }
  }

  public void deleteById(Long id) {
    customerRepository.deleteById(id);
  }

  private void resolveUniqueConstraint(DataIntegrityViolationException ex)
      throws CustomerUniqueViolationException {
    String constraint = constraintResolver.resolveDisplayName(ex, CustomerConstraintNames.class);
    throw new CustomerUniqueViolationException(constraint);
  }
}
