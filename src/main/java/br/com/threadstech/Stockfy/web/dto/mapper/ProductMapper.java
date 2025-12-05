package br.com.threadstech.stockfy.web.dto.mapper;

import br.com.threadstech.stockfy.entity.Product;
import br.com.threadstech.stockfy.web.dto.ProductCreateDto;
import br.com.threadstech.stockfy.web.dto.ProductResponseDto;
import br.com.threadstech.stockfy.web.dto.ProductUpdateDto;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;
import org.springframework.data.domain.Page;

@Mapper
public interface ProductMapper {

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "deleted", ignore = true)
  Product toProduct(ProductCreateDto productDto);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "deleted", ignore = true)
  void updateProduct(ProductUpdateDto productUpdateDto, @MappingTarget Product product);

  @BeanMapping(unmappedSourcePolicy = ReportingPolicy.IGNORE)
  ProductResponseDto toDto(Product product);

  default Page<ProductResponseDto> toPageDto(Page<Product> productPage) {
    return productPage.map(this::toDto);
  }
}
