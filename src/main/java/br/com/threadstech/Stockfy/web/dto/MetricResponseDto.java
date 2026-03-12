package br.com.threadstech.stockfy.web.dto;

import br.com.threadstech.stockfy.enums.MetricI18nKeys;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MetricResponseDto {
  private String type;
  private MetricI18nKeys i18nKey;
  private Double value;
  private Double percentage;
}
