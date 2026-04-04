package br.com.threadstech.stockfy.modules.product.dto.mapper;

import br.com.threadstech.stockfy.modules.product.ProductV1_1;
import br.com.threadstech.stockfy.modules.product.dto.ProductCreateDto;
import br.com.threadstech.stockfy.modules.product.dto.ProductDetailsDto;
import br.com.threadstech.stockfy.modules.product.dto.ProductResponseV1_1;
import br.com.threadstech.stockfy.modules.product.dto.ProductSaleDto;
import br.com.threadstech.stockfy.modules.product.dto.ProductSearchDto;
import br.com.threadstech.stockfy.modules.product.dto.ProductSummaryDto;
import br.com.threadstech.stockfy.modules.product.utils.StockStatusResolver;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;

@Mapper
public abstract class ProductV1_1Mapper {

  @Autowired protected StockStatusResolver stockStatusResolver;

  @Mapping(target = "id", ignore = true)
  public abstract ProductV1_1 toProduct(ProductCreateDto productDto);

  @BeanMapping(unmappedSourcePolicy = ReportingPolicy.IGNORE)
  public abstract ProductDetailsDto toProductDetailsDto(ProductV1_1 product);

  @Mapping(
      target = "stockStatus",
      expression = "java(stockStatusResolver.resolve(product.getStockQuantity(), product.getMinimumStock()))")
  @BeanMapping(unmappedSourcePolicy = ReportingPolicy.IGNORE)
  public abstract ProductSummaryDto toProductSummaryDto(ProductV1_1 product);

  @BeanMapping(unmappedSourcePolicy = ReportingPolicy.IGNORE)
  public abstract ProductSearchDto toProductSearchDto(ProductV1_1 product);

  @BeanMapping(unmappedSourcePolicy = ReportingPolicy.IGNORE)
  public abstract ProductSaleDto toProductSaleDto(ProductV1_1 product);

  public Page<ProductSummaryDto> toProductSummaryDtoPage(Page<ProductV1_1> productPage) {
    return productPage.map(this::toProductSummaryDto);
  }

  public Page<ProductSearchDto> toProductSearchDtoPage(Page<ProductV1_1> productPage) {
    return productPage.map(this::toProductSearchDto);
  }

  public Page<ProductSaleDto> toProductSaleDtoPage(Page<ProductV1_1> productPage) {
    return productPage.map(this::toProductSaleDto);
  }
}
