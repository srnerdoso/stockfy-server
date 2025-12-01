package br.com.threadstech.stockfy.web.dto.mapper;

import org.modelmapper.ModelMapper;

import br.com.threadstech.stockfy.entity.Product;
import br.com.threadstech.stockfy.web.dto.ProductDto;
import br.com.threadstech.stockfy.web.dto.ProductResponseDto;

public class ProductMapper {

  private ProductMapper() {
  }

  public static Product toProduct(ProductDto dto) {
    return new ModelMapper().map(dto, Product.class);
  }

  public static ProductResponseDto toDto(Product product) {
    return new ModelMapper().map(product, ProductResponseDto.class);
  }
}
