package br.com.threadstech.stockfy.web.controller;

import br.com.threadstech.stockfy.api.ApiPaths;
import br.com.threadstech.stockfy.service.ProductService;
import br.com.threadstech.stockfy.web.dto.ProductDto;
import br.com.threadstech.stockfy.web.dto.ProductResponseDto;
import br.com.threadstech.stockfy.web.dto.groups.Create;
import br.com.threadstech.stockfy.web.dto.groups.Update;
import br.com.threadstech.stockfy.web.dto.mapper.ProductMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ApiPaths.PRODUCT)
@RequiredArgsConstructor
public class ProductController {

  private final ProductService productService;

  @PostMapping
  public ResponseEntity<Void> save(@Validated(Create.class) @RequestBody ProductDto product) {
    productService.save(ProductMapper.toProduct(product));
    return ResponseEntity.status(HttpStatus.CREATED).build();
  }

  @GetMapping
  public ResponseEntity<Page<ProductResponseDto>> findAll(@PageableDefault Pageable pageable) {
    return ResponseEntity.ok(ProductMapper.toPageDto(productService.findAll(pageable)));
  }

  @GetMapping("/{barCode}/barcode")
  public ResponseEntity<ProductResponseDto> findByBarCode(@PathVariable String barCode) {
    return ResponseEntity.ok(ProductMapper.toDto(productService.findByBarCode(barCode)));
  }

  @GetMapping("/{name}/name")
  public ResponseEntity<Page<ProductResponseDto>> findAllByName(
      @PathVariable String name, @PageableDefault Pageable pageable) {
    return ResponseEntity.ok(ProductMapper.toPageDto(productService.findAllByName(name, pageable)));
  }

  @PatchMapping("/{id}/id")
  public ResponseEntity<Void> updateProductById(
      @PathVariable Long id, @Validated(Update.class) @RequestBody ProductDto productDto) {
    productService.updateById(id, productDto);
    return ResponseEntity.noContent().build();
  }

  @DeleteMapping("/{id}/id")
  public ResponseEntity<Void> deleteProductById(@PathVariable Long id) {
    productService.deleteById(id);
    return ResponseEntity.noContent().build();
  }
}
