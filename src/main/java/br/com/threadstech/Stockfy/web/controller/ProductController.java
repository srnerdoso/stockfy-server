package br.com.threadstech.stockfy.web.controller;

import br.com.threadstech.stockfy.api.ApiPaths;
import br.com.threadstech.stockfy.service.ProductService;
import br.com.threadstech.stockfy.web.doc.ProductControllerDoc;
import br.com.threadstech.stockfy.web.dto.ProductCreateDto;
import br.com.threadstech.stockfy.web.dto.ProductDetailResponseDto;
import br.com.threadstech.stockfy.web.dto.ProductSummaryResponseDto;
import br.com.threadstech.stockfy.web.dto.ProductUpdateDto;
import br.com.threadstech.stockfy.web.dto.mapper.ProductMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
public class ProductController implements ProductControllerDoc {

  private final ProductService productService;
  private final ProductMapper productMapper;

  @PostMapping
  public ResponseEntity<Void> save(@Valid @RequestBody ProductCreateDto product) {
    productService.save(productMapper.toProduct(product));
    return ResponseEntity.status(HttpStatus.CREATED).build();
  }

  @GetMapping
  public ResponseEntity<Page<ProductSummaryResponseDto>> findAll(
      @PageableDefault Pageable pageable) {
    return ResponseEntity.ok(productMapper.toPageDto(productService.findAll(pageable)));
  }

  @GetMapping("/{barCode}/barcode")
  public ResponseEntity<ProductDetailResponseDto> findByBarCode(@PathVariable String barCode) {
    return ResponseEntity.ok(productMapper.toDetailDto(productService.findByBarCode(barCode)));
  }

  @GetMapping("/{name}/name")
  public ResponseEntity<Page<ProductSummaryResponseDto>> findAllByName(
      @PathVariable String name, @PageableDefault Pageable pageable) {
    return ResponseEntity.ok(productMapper.toPageDto(productService.findAllByName(name, pageable)));
  }

  @PatchMapping("/{id}/id")
  public ResponseEntity<Void> updateProductById(
      @PathVariable Long id, @Valid @RequestBody ProductUpdateDto productDto) {
    productService.updateById(id, productDto, productMapper);
    return ResponseEntity.noContent().build();
  }

  @DeleteMapping("/{id}/id")
  public ResponseEntity<Void> deleteProductById(@PathVariable Long id) {
    productService.deleteById(id);
    return ResponseEntity.noContent().build();
  }
}
