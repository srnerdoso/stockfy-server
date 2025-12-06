package br.com.threadstech.stockfy.web.controller;

import br.com.threadstech.stockfy.api.ApiPaths;
import br.com.threadstech.stockfy.entity.Customer;
import br.com.threadstech.stockfy.service.CustomerService;
import br.com.threadstech.stockfy.web.dto.CustomerCreateDto;
import br.com.threadstech.stockfy.web.dto.mapper.CustomerMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
}
