package br.com.threadstech.stockfy.web.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.threadstech.stockfy.api.ApiPaths;
import br.com.threadstech.stockfy.service.ProductService;
import br.com.threadstech.stockfy.web.dto.ProductDto;
import br.com.threadstech.stockfy.web.dto.ProductResponseDto;
import br.com.threadstech.stockfy.web.dto.mapper.ProductMapper;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(ApiPaths.PRODUCT)
@RequiredArgsConstructor
public class ProductController {

  private final ProductService productService;

  @PostMapping
  public ResponseEntity<Void> save(@RequestBody ProductDto product) {
    productService.save(ProductMapper.toProduct(product));
    return ResponseEntity.status(HttpStatus.CREATED).build();
  }

  @GetMapping("/{barCode}")
  public ResponseEntity<ProductResponseDto> findByBarCode(@PathVariable String barCode) {
    return ResponseEntity.ok(ProductMapper.toDto(productService.findByBarCode(barCode)));
  }
}
