package br.com.threadstech.stockfy.web.controller;

import br.com.threadstech.stockfy.api.ApiPaths;
import br.com.threadstech.stockfy.entity.Product;
import br.com.threadstech.stockfy.service.PaymentService;
import br.com.threadstech.stockfy.web.dto.PaymentCreateDto;
import br.com.threadstech.stockfy.web.dto.mapper.PaymentMapper;
import java.util.Set;
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
@RequestMapping(ApiPaths.PAYMENT)
@RequiredArgsConstructor
public class PaymentController {

  private final PaymentService paymentService;

  @PostMapping
  public ResponseEntity<Void> save(@RequestBody PaymentCreateDto paymentDto) {
    Set<Product> products = paymentService.getProductsFromCarts(paymentDto.getCart());
    paymentService.save(PaymentMapper.toPayment(paymentDto, products));
    return ResponseEntity.status(HttpStatus.CREATED).build();
  }
}
