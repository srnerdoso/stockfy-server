package br.com.threadstech.stockfy.web.dto.mapper;

import br.com.threadstech.stockfy.entity.Product;
import br.com.threadstech.stockfy.web.dto.ProductDto;
import br.com.threadstech.stockfy.web.dto.ProductResponseDto;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;

// TODO: Trocar para MapStruct para manter padrão no projeto e melhorar performance
public class ProductMapper {

  private static final ModelMapper mapper;

  static {
    mapper = new ModelMapper();
    mapper.getConfiguration().setSkipNullEnabled(true);
  }

  public static Product toProduct(ProductDto dto) {
    return mapper.map(dto, Product.class);
  }

  public static ProductResponseDto toDto(Product product) {
    return mapper.map(product, ProductResponseDto.class);
  }

  public static Product updateProductByDto(ProductDto dto, Product oldProduct) {
    Product newProduct = toProduct(dto);
    mapper.map(newProduct, oldProduct);
    return oldProduct;
  }

  public static Page<ProductResponseDto> toPageDto(Page<Product> all) {
    return all.map(ProductMapper::toDto);
  }
}
