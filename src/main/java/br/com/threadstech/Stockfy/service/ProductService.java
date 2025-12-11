package br.com.threadstech.stockfy.service;

import br.com.threadstech.stockfy.components.ConstraintResolver;
import br.com.threadstech.stockfy.config.constraints.EmployeeConstraintNames;
import br.com.threadstech.stockfy.config.constraints.ProductConstraintNames;
import br.com.threadstech.stockfy.entity.Product;
import br.com.threadstech.stockfy.exception.EmployeeUniqueViolationException;
import br.com.threadstech.stockfy.exception.EntityNotFoundException;
import br.com.threadstech.stockfy.exception.ProductUniqueViolationException;
import br.com.threadstech.stockfy.repository.ProductRepository;
import br.com.threadstech.stockfy.web.dto.ProductUpdateDto;
import br.com.threadstech.stockfy.web.dto.mapper.ProductMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

  private final ProductRepository productRepository;
  private final ConstraintResolver constraintResolver;

  @Transactional
  public void save(Product product) {
    log.info("Saving product: {}", product.getName());
    try {
      productRepository.save(product);
    } catch (DataIntegrityViolationException ex) {
      resolveUniqueConstraint(ex);
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

  public void updateById(Long id, ProductUpdateDto productDto, ProductMapper productMapper) {
    log.info(
        """
        Updating product by id:
          id={};
          dto={}
        """,
        id,
        productDto.toString());
    try {
      Product product = findById(id);
      productMapper.updateProduct(productDto, product);
    } catch (DataIntegrityViolationException ex) {
      resolveUniqueConstraint(ex);
    }
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

  private void resolveUniqueConstraint(DataIntegrityViolationException ex)
      throws ProductUniqueViolationException {
    String constraint = constraintResolver.resolveDisplayName(ex, ProductConstraintNames.class);
    throw new ProductUniqueViolationException(constraint);
  }
}
