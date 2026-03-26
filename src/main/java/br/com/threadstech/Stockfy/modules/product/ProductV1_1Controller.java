package br.com.threadstech.stockfy.modules.product;

import br.com.threadstech.stockfy.api.ApiPaths;
import br.com.threadstech.stockfy.modules.product.dto.ProductCreateDto;
import br.com.threadstech.stockfy.modules.product.dto.ProductDetailsDto;
import br.com.threadstech.stockfy.modules.product.dto.mapper.ProductV1_1Mapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ApiPaths.PRODUCT_V1_1)
@RequiredArgsConstructor
public class ProductV1_1Controller implements ProductV1_1ControllerDoc {

  private final ProductV1_1Service productService;
  private final ProductV1_1Mapper productMapper;

  @Override
  @PostMapping
  @PreAuthorize("hasAnyRole('ADMIN', 'INVENTORY_MANAGER')")
  public ResponseEntity<Void> save(@Valid @RequestBody ProductCreateDto productDto) {
    productService.createProduct(productMapper.toProduct(productDto));
    return ResponseEntity.status(HttpStatus.CREATED).build();
  }

  @Override
  @GetMapping("/{id}/id")
  @PreAuthorize("hasAnyRole('ADMIN', 'INVENTORY_MANAGER', 'SALES_ATTENDANT')")
  public ResponseEntity<ProductDetailsDto> getById(@PathVariable Long id) {
    return ResponseEntity.ok(productMapper.toProductDetailsDto(productService.findById(id)));
  }
}
