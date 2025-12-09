package br.com.threadstech.stockfy.web.controller;

import br.com.threadstech.stockfy.api.ApiPaths;
import br.com.threadstech.stockfy.service.PaymentService;
import br.com.threadstech.stockfy.web.doc.PaymentControllerDoc;
import br.com.threadstech.stockfy.web.dto.PaymentCreateDto;
import br.com.threadstech.stockfy.web.dto.mapper.PaymentMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping(ApiPaths.PAYMENT)
@RequiredArgsConstructor
public class PaymentController implements PaymentControllerDoc {

  private final PaymentService paymentService;
  private final PaymentMapper paymentMapper;

  @PostMapping("/pay")
  public ResponseEntity<Void> pay(@Valid @RequestBody PaymentCreateDto paymentDto) {
    log.info(
        "PaymentController - Converting PaymentCreateDto to Payment and saving: {}",
        paymentDto.toString());
    paymentService.save(paymentMapper.toPayment(paymentDto));
    return ResponseEntity.status(HttpStatus.CREATED).build();
  }

  @PostMapping("/{id}/refund")
  public ResponseEntity<Void> refund(@PathVariable Long id) {
    paymentService.refund(id);
    return ResponseEntity.noContent().build();
  }
}
