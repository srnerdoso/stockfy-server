package br.com.threadstech.stockfy.modules.product;

import br.com.threadstech.stockfy.components.ConstraintResolver;
import br.com.threadstech.stockfy.exception.EntityNotFoundException;
import br.com.threadstech.stockfy.modules.product.dto.ProductSummaryDto;
import br.com.threadstech.stockfy.modules.product.dto.mapper.ProductV1_1Mapper;
import br.com.threadstech.stockfy.modules.product.exception.ProductUniqueViolationException;
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
public class ProductV1_1Service {

  private final ProductV1_1Repository productRepository;
  private final ProductV1_1Mapper productMapper;
  private final ConstraintResolver constraintResolver;

  @Transactional(readOnly = true)
  public Page<ProductSummaryDto> findProducts(String name, Pageable pageable) {
    log.info("Finding products with name: {} and pagination: {}", name, pageable);
    Page<ProductV1_1> productPage;
    if (name != null && !name.isBlank()) {
      productPage = productRepository.findByNameContainingIgnoreCase(name, pageable);
    } else {
      productPage = productRepository.findAll(pageable);
    }
    return productMapper.toProductSummaryDtoPage(productPage);
  }

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
  public ProductV1_1 findByBarcode(String barcode) {
    log.info("Finding product by barcode: {}", barcode);
    return productRepository
        .findByBarcode(barcode)
        .orElseThrow(() -> new EntityNotFoundException(barcode));
  }

  private void resolveUniqueConstraint(DataIntegrityViolationException ex) {
    String constraint = constraintResolver.resolveDisplayName(ex, ProductV1_1ConstraintNames.class);
    if (constraint != null) {
      throw new ProductUniqueViolationException(constraint);
    }
  }
}
