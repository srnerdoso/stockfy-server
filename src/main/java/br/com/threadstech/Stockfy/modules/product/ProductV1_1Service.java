package br.com.threadstech.stockfy.modules.product;

import br.com.threadstech.stockfy.components.ConstraintResolver;
import br.com.threadstech.stockfy.exception.EntityNotFoundException;
import br.com.threadstech.stockfy.modules.product.exception.ProductUniqueViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductV1_1Service {

  private final ProductV1_1Repository productRepository;
  private final ConstraintResolver constraintResolver;

  @Transactional
  public ProductV1_1 createProduct(ProductV1_1 product) {
    log.info("Creating product: {}", product.getName());
    try {
      return productRepository.save(product);
    } catch (DataIntegrityViolationException ex) {
      resolveUniqueConstraint(ex);
      throw ex;
    }
  }

  @Transactional(readOnly = true)
  public ProductV1_1 findById(Long id) {
    log.info("Finding product by id: {}", id);
    return productRepository
        .findById(id)
        .orElseThrow(() -> new EntityNotFoundException(id.toString()));
  }

  private void resolveUniqueConstraint(DataIntegrityViolationException ex) {
    String constraint = constraintResolver.resolveDisplayName(ex, ProductV1_1ConstraintNames.class);
    if (constraint != null) {
      throw new ProductUniqueViolationException(constraint);
    }
  }
}
