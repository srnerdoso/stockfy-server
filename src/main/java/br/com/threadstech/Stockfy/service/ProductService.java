package br.com.threadstech.stockfy.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import br.com.threadstech.stockfy.entity.Product;
import br.com.threadstech.stockfy.exception.EntityNotFoundException;
import br.com.threadstech.stockfy.repository.ProductRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

  private final ProductRepository productRepository;

  public Product save(Product product) {
    return productRepository.save(product);
  }

  public Product findByBarCode(String barCode) {
    return productRepository
        .findByBarCode(barCode)
        .orElseThrow(() -> new EntityNotFoundException("Product not found"));
  }
}
