package br.com.threadstech.stockfy.web.controller;

import br.com.threadstech.stockfy.api.ApiPaths;
import br.com.threadstech.stockfy.entity.Customer;
import br.com.threadstech.stockfy.service.CustomerService;
import br.com.threadstech.stockfy.web.dto.CustomerContactDetailDto;
import br.com.threadstech.stockfy.web.dto.CustomerCreateDto;
import br.com.threadstech.stockfy.web.dto.CustomerDetailDto;
import br.com.threadstech.stockfy.web.dto.CustomerSummaryDto;
import br.com.threadstech.stockfy.web.dto.mapper.CustomerMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping(ApiPaths.CUSTOMER)
@RequiredArgsConstructor
public class CustomerController {

  private final CustomerService customerService;
  private final CustomerMapper customerMapper;

  @PostMapping
  public ResponseEntity<Void> save(@Valid @RequestBody CustomerCreateDto customerDto) {
    log.info("Converting CustomerCreateDto to Customer and saving: {}", customerDto.toString());
    customerService.save(customerMapper.toCustomer(customerDto));
    return ResponseEntity.status(HttpStatus.CREATED).build();
  }

  @GetMapping
  public ResponseEntity<Page<CustomerSummaryDto>> findAll(@PageableDefault Pageable pageable) {
    Page<Customer> customers = customerService.findAll(pageable);
    return ResponseEntity.ok(customerMapper.toPageSummary(customers));
  }

  @GetMapping("/{id}")
  public ResponseEntity<CustomerDetailDto> findById(@PathVariable Long id) {
    Customer customer = customerService.findById(id);
    return ResponseEntity.ok(customerMapper.toDetail(customer));
  }

  @GetMapping("/{id}/cpf")
  public ResponseEntity<String> findCpfById(@PathVariable Long id) {
    String cpf = customerService.findCpfById(id);
    return ResponseEntity.ok(cpf);
  }
}
