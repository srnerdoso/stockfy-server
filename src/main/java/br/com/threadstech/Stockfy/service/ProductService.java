package br.com.threadstech.stockfy.service;

import br.com.threadstech.stockfy.entity.Product;
import br.com.threadstech.stockfy.exception.EntityNotFoundException;
import br.com.threadstech.stockfy.exception.ProductUniqueViolationException;
import br.com.threadstech.stockfy.repository.ProductRepository;
import br.com.threadstech.stockfy.web.dto.ProductCreateDto;
import br.com.threadstech.stockfy.web.dto.ProductUpdateDto;
import br.com.threadstech.stockfy.web.dto.mapper.ProductMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

  private final MessageSource messageSource;
  private final ProductRepository productRepository;

  @Transactional
  public void save(Product product) {
    log.info("Saving product: {}", product.getName());
    try {
      productRepository.save(product);
    } catch (org.springframework.dao.DataIntegrityViolationException ex) {
      throw new ProductUniqueViolationException(product.getBarCode());
    }
  }

  @Transactional(readOnly = true)
  public Product findByBarCode(String barCode) {
    log.info("Finding product by bar code: {}", barCode);
    return productRepository
        .findByBarCode(barCode)
        .orElseThrow(() -> new EntityNotFoundException(barCode));
  }

  @Transactional(readOnly = true)
  public Page<Product> findAllByName(String name, Pageable pageable) {
    log.info("Finding product by name: {}", name);
    return productRepository.findAllByName(name, pageable);
  }

  @Transactional(readOnly = true)
  public Product findById(Long id) {
    log.info("Finding product by id: {}", id);
    return productRepository
        .findById(id)
        .orElseThrow(() -> new EntityNotFoundException(id.toString()));
  }

  @Transactional
  public void updateById(Long id, ProductUpdateDto productDto, ProductMapper productMapper) {
    log.info(
        """
        Updating product by id:
          id={};
          dto={}
        """,
        id,
        productDto.toString());
    Product product = findById(id);
    productMapper.updateProduct(productDto, product);
    log.info("Product id={} updated successfully.", id);
  }

  public void deleteById(Long id) {
    log.info("Deleting product by id: {}...", id);
    productRepository.deleteById(id);
    log.info("Deleted product successfully!");
  }

  public Page<Product> findAll(Pageable pageable) {
    return productRepository.findAll(pageable);
  }
}
