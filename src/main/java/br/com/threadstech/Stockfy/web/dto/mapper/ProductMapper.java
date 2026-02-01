package br.com.threadstech.stockfy.web.dto.mapper;

import br.com.threadstech.stockfy.entity.Product;
import br.com.threadstech.stockfy.web.dto.*;
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
  ProductSummaryResponseDto toSummaryDto(Product product);

  default Page<ProductSummaryResponseDto> toPageDto(Page<Product> productPage) {
    return productPage.map(this::toSummaryDto);
  }

  @BeanMapping(unmappedSourcePolicy = ReportingPolicy.IGNORE)
  ProductDetailResponseDto toDetailDto(Product product);

  @BeanMapping(unmappedSourcePolicy = ReportingPolicy.IGNORE)
  ProductAutocompleteResponseDto toAutocompleteDto(Product product);

  default Page<ProductAutocompleteResponseDto> toAutocompletePageDto(Page<Product> productPage) {
    return productPage.map(this::toAutocompleteDto);
  }
}
