package br.com.threadstech.stockfy.service;

import br.com.threadstech.stockfy.exception.UniqueFieldViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import br.com.threadstech.stockfy.entity.Product;
import br.com.threadstech.stockfy.exception.EntityNotFoundException;
import br.com.threadstech.stockfy.repository.ProductRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

  private final MessageSource messageSource;
  private final ProductRepository productRepository;

  public Product save(Product product) {
    try {
      return productRepository.save(product);
    } catch (org.springframework.dao.DataIntegrityViolationException ex) {
      throw new UniqueFieldViolationException(
          messageSource.getMessage(
              "UniqueFieldViolationException.product",
              null,
              LocaleContextHolder.getLocale()));
    }
  }

  public Product findByBarCode(String barCode) {
    return productRepository
        .findByBarCode(barCode)
        .orElseThrow(() -> new EntityNotFoundException("Product not found"));
  }
}
