package br.com.threadstech.stockfy.service;

import br.com.threadstech.stockfy.entity.Customer;
import br.com.threadstech.stockfy.exception.CustomerUniqueViolationException;
import br.com.threadstech.stockfy.exception.EntityNotFoundException;
import br.com.threadstech.stockfy.repository.CustomerRepository;
import br.com.threadstech.stockfy.web.dto.CustomerCreateDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerService {

  private final CustomerRepository customerRepository;

  public void save(Customer customer) {
    try {
      Customer customerSaved = customerRepository.save(customer);
      log.info("Customer saved successfully with id={}", customerSaved.getId());
    } catch (DataIntegrityViolationException ex) {
      String constraint = "unknown";
      if (ex.getCause() instanceof org.hibernate.exception.ConstraintViolationException cve) {
        constraint = cve.getConstraintName();
      }
      throw new CustomerUniqueViolationException(constraint);
    }
  }

  public Customer findById(Long id) {
    return customerRepository
        .findById(id)
        .orElseThrow(() -> new EntityNotFoundException(id.toString()));
  }

  public Page<Customer> findAll(Pageable pageable) {
    return customerRepository.findAll(pageable);
  }

  public String findCpfById(Long id) {
    return customerRepository
        .findCpfById(id)
        .orElseThrow(() -> new EntityNotFoundException(id.toString()));
  }
}
