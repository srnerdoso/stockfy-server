package br.com.threadstech.stockfy.modules.product;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.threadstech.stockfy.components.ConstraintResolver;
import br.com.threadstech.stockfy.modules.product.dto.ProductResponseV1_1;
import br.com.threadstech.stockfy.modules.product.dto.ProductSummaryDto;
import br.com.threadstech.stockfy.modules.product.dto.ProductSearchDto;
import br.com.threadstech.stockfy.modules.product.dto.ProductSaleDto;
import br.com.threadstech.stockfy.modules.product.dto.mapper.ProductV1_1Mapper;
import br.com.threadstech.stockfy.modules.product.enums.ProductResponseType;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class ProductServiceUnitTests {

  @Mock private ProductV1_1Repository productRepository;
  @Mock private ProductV1_1Mapper productMapper;
  @Mock private ConstraintResolver constraintResolver;

  @InjectMocks private ProductV1_1Service productService;

  @Test
  @DisplayName("Should call Summary strategy when type is SUMMARY")
  void shouldCallSummaryStrategyWhenTypeIsSummary() {
    Pageable pageable = PageRequest.of(0, 10);
    ProductV1_1 product = new ProductV1_1();
    Page<ProductV1_1> productPage = new PageImpl<>(List.of(product));
    
    when(productRepository.findAll(pageable)).thenReturn(productPage);
    when(productMapper.toProductSummaryDtoPage(productPage)).thenReturn(new PageImpl<>(List.of(new ProductSummaryDto())));

    Page<? extends ProductResponseV1_1> result = productService.findProducts(null, ProductResponseType.SUMMARY, pageable);

    assertThat(result.getContent()).hasSize(1);
    verify(productMapper).toProductSummaryDtoPage(productPage);
  }

  @Test
  @DisplayName("Should call Search strategy when type is SEARCH")
  void shouldCallSearchStrategyWhenTypeIsSearch() {
    Pageable pageable = PageRequest.of(0, 10);
    ProductV1_1 product = new ProductV1_1();
    Page<ProductV1_1> productPage = new PageImpl<>(List.of(product));

    when(productRepository.findAll(pageable)).thenReturn(productPage);
    when(productMapper.toProductSearchDtoPage(productPage)).thenReturn(new PageImpl<>(List.of(new ProductSearchDto())));

    Page<? extends ProductResponseV1_1> result = productService.findProducts(null, ProductResponseType.SEARCH, pageable);

    assertThat(result.getContent()).hasSize(1);
    verify(productMapper).toProductSearchDtoPage(productPage);
  }

  @Test
  @DisplayName("Should call Sale strategy when type is SALE")
  void shouldCallSaleStrategyWhenTypeIsSale() {
    Pageable pageable = PageRequest.of(0, 10);
    ProductV1_1 product = new ProductV1_1();
    Page<ProductV1_1> productPage = new PageImpl<>(List.of(product));

    when(productRepository.findAll(pageable)).thenReturn(productPage);
    when(productMapper.toProductSaleDtoPage(productPage)).thenReturn(new PageImpl<>(List.of(new ProductSaleDto())));

    Page<? extends ProductResponseV1_1> result = productService.findProducts(null, ProductResponseType.SALE, pageable);

    assertThat(result.getContent()).hasSize(1);
    verify(productMapper).toProductSaleDtoPage(productPage);
  }

  @Test
  @DisplayName("Should filter by name when name is provided")
  void shouldFilterByNameWhenNameIsProvided() {
    String name = "notebook";
    Pageable pageable = PageRequest.of(0, 10);
    Page<ProductV1_1> productPage = new PageImpl<>(Collections.emptyList());

    when(productRepository.findByNameContainingIgnoreCase(eq(name), any(Pageable.class)))
        .thenReturn(productPage);
    when(productMapper.toProductSummaryDtoPage(any())).thenReturn(new PageImpl<>(Collections.emptyList()));

    productService.findProducts(name, ProductResponseType.SUMMARY, pageable);

    verify(productRepository).findByNameContainingIgnoreCase(eq(name), any(Pageable.class));
  }
}
