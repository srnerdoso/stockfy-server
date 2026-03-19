package br.com.threadstech.stockfy.web.dto.mapper;

import br.com.threadstech.stockfy.entity.Alert;
import br.com.threadstech.stockfy.web.dto.AlertDto;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedSourcePolicy = ReportingPolicy.IGNORE)
public interface AlertMapper {

  @Mapping(target = "product", source = "product.name")
  @Mapping(target = "productType", source = "product.type")
  @Mapping(target = "stock", source = "product.stock")
  AlertDto toDto(Alert alert);

  List<AlertDto> toDtoList(List<Alert> alerts);
}
