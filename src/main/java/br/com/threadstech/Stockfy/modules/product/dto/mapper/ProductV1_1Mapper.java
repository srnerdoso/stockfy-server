package br.com.threadstech.stockfy.modules.product.dto.mapper;

import br.com.threadstech.stockfy.modules.product.ProductV1_1;
import br.com.threadstech.stockfy.modules.product.dto.ProductCreateDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface ProductV1_1Mapper {

  @Mapping(target = "id", ignore = true)
  ProductV1_1 toProduct(ProductCreateDto productDto);
}
