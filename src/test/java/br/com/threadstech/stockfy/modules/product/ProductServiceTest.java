package br.com.threadstech.stockfy.modules.product;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.threadstech.stockfy.modules.product.dto.ProductCreateDto;
import br.com.threadstech.stockfy.modules.product.dto.ProductResponseDto;
import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

  @Mock private ProductRepository productRepository;

  @InjectMocks private ProductService productService;

  @Test
  @DisplayName("shouldCreateProductReturnProductResponseDto")
  void shouldCreateProductReturnProductResponseDto() {
    ProductCreateDto createDto =
        ProductCreateDto.builder()
            .barcode(ProductTestData.BARCODE)
            .name(ProductTestData.NAME)
            .stock(ProductTestData.STOCK)
            .minStock(ProductTestData.MIN_STOCK)
            .price(ProductTestData.PRICE)
            .cost(ProductTestData.COST)
            .discount(ProductTestData.DISCOUNT)
            .type(ProductTestData.TYPE)
            .build();

    when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

    productService.createProduct(createDto);

    ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
    verify(productRepository).save(productCaptor.capture());

    Product savedProduct = productCaptor.getValue();
    assertThat(savedProduct.getBarcode()).isEqualTo(createDto.getBarcode());
    assertThat(savedProduct.getName()).isEqualTo(createDto.getName());
    assertThat(savedProduct.getPrice()).isEqualTo(createDto.getPrice());
    assertThat(savedProduct.getCost()).isEqualTo(createDto.getCost());
    assertThat(savedProduct.getProfit()).isEqualTo(new BigDecimal("50.00")); // price - cost
    assertThat(savedProduct.getDeleted()).isFalse();
  }

  @Test
  @DisplayName("shouldThrowExceptionWhenPriceIsLessThanCost")
  void shouldThrowExceptionWhenPriceIsLessThanCost() {
    ProductCreateDto createDto =
        ProductCreateDto.builder()
            .barcode(ProductTestData.BARCODE)
            .name(ProductTestData.NAME)
            .stock(10)
            .minStock(5)
            .price(new BigDecimal("40.00"))
            .cost(new BigDecimal("50.00"))
            .type("ELECTRONICS")
            .build();

    assertThatThrownBy(() -> productService.createProduct(createDto))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Price must be greater than or equal to cost");
  }

  @Test
  @DisplayName("shouldThrowExceptionWhenDiscountIsGreaterThanPrice")
  void shouldThrowExceptionWhenDiscountIsGreaterThanPrice() {
    ProductCreateDto createDto =
        ProductCreateDto.builder()
            .barcode(ProductTestData.BARCODE)
            .name(ProductTestData.NAME)
            .stock(10)
            .minStock(5)
            .price(new BigDecimal("100.00"))
            .cost(new BigDecimal("50.00"))
            .discount(new BigDecimal("110.00"))
            .type("ELECTRONICS")
            .build();

    assertThatThrownBy(() -> productService.createProduct(createDto))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Discount cannot be greater than price");
  }
}
