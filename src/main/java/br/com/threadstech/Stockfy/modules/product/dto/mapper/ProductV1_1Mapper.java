package br.com.threadstech.stockfy.modules.product.dto.mapper;

import br.com.threadstech.stockfy.modules.product.ProductV1_1;
import br.com.threadstech.stockfy.modules.product.dto.ProductCreateDto;
import br.com.threadstech.stockfy.modules.product.dto.ProductDetailsDto;
import br.com.threadstech.stockfy.modules.product.dto.ProductSummaryDto;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.springframework.data.domain.Page;

@Mapper
public interface ProductV1_1Mapper {

  @Mapping(target = "id", ignore = true)
  ProductV1_1 toProduct(ProductCreateDto productDto);

  @BeanMapping(unmappedSourcePolicy = ReportingPolicy.IGNORE)
  ProductDetailsDto toProductDetailsDto(ProductV1_1 product);

  @BeanMapping(unmappedSourcePolicy = ReportingPolicy.IGNORE)
  ProductSummaryDto toProductSummaryDto(ProductV1_1 product);

  default Page<ProductSummaryDto> toProductSummaryDtoPage(Page<ProductV1_1> productPage) {
    return productPage.map(this::toProductSummaryDto);
  }
}
