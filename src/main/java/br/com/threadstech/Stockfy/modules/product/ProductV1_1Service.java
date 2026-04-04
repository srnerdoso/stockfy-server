package br.com.threadstech.stockfy.modules.product;

import br.com.threadstech.stockfy.components.ConstraintResolver;
import br.com.threadstech.stockfy.exception.EntityNotFoundException;
import br.com.threadstech.stockfy.modules.product.dto.ProductResponseV1_1;
import br.com.threadstech.stockfy.modules.product.dto.mapper.ProductV1_1Mapper;
import br.com.threadstech.stockfy.modules.product.enums.ProductResponseType;
import br.com.threadstech.stockfy.modules.product.exception.ProductUniqueViolationException;
import java.util.EnumMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class ProductV1_1Service {

  private final ProductV1_1Repository productRepository;
  private final ProductV1_1Mapper productMapper;
  private final ConstraintResolver constraintResolver;
  private final Map<ProductResponseType, ProductResponseTypeStrategy> strategies;

  public ProductV1_1Service(
      ProductV1_1Repository productRepository,
      ProductV1_1Mapper productMapper,
      ConstraintResolver constraintResolver) {
    this.productRepository = productRepository;
    this.productMapper = productMapper;
    this.constraintResolver = constraintResolver;
    this.strategies = new EnumMap<>(ProductResponseType.class);
    this.initializeStrategies();
  }

  private void initializeStrategies() {
    strategies.put(ProductResponseType.SUMMARY, productMapper::toProductSummaryDtoPage);
    strategies.put(ProductResponseType.SEARCH, productMapper::toProductSearchDtoPage);
    strategies.put(ProductResponseType.SALE, productMapper::toProductSaleDtoPage);
  }

  @Transactional(readOnly = true)
  public Page<? extends ProductResponseV1_1> findProducts(
      String name, ProductResponseType type, Pageable pageable) {
    log.info("Finding products with name: {}, type: {} and pagination: {}", name, type, pageable);

    Page<ProductV1_1> productPage = fetchProducts(name, pageable);

    return strategies.get(type).map(productPage);
  }

  private Page<ProductV1_1> fetchProducts(String name, Pageable pageable) {
    if (name != null && !name.isBlank()) {
      return productRepository.findByNameContainingIgnoreCase(name, pageable);
    }
    return productRepository.findAll(pageable);
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

  @FunctionalInterface
  private interface ProductResponseTypeStrategy {
    Page<? extends ProductResponseV1_1> map(Page<ProductV1_1> productPage);
  }
}
