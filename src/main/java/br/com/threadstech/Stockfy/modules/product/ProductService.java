package br.com.threadstech.stockfy.modules.product;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Serviço responsável por gerenciar os produtos.
 *
 * @author threadstech
 * @see Product
 * @see ProductRepository
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

  private final ProductRepository productRepository;

  /**
   * Salva um produto no banco de dados.
   *
   * @param product o produto a ser salvo
   * @return o produto salvo
   * @see Product
   * @see ProductRepository#save(Object)
   */
  public Product save(Product product) {
    return productRepository.save(product);
  }
}
