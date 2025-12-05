package br.com.threadstech.stockfy.service;

import br.com.threadstech.stockfy.entity.Customer;
import br.com.threadstech.stockfy.exception.EntityNotFoundException;
import br.com.threadstech.stockfy.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerService {

  private final CustomerRepository customerRepository;

  public Customer findById(Long id) {
    return customerRepository
        .findById(id)
        .orElseThrow(() -> new EntityNotFoundException(id.toString()));
  }
}
