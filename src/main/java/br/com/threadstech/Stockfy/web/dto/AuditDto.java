package br.com.threadstech.stockfy.web.dto;

import br.com.threadstech.stockfy.enums.AuditI18nKeys;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditDto {
  private Long actionId;
  private AuditI18nKeys i18nKey;
  private String timestamp;
  private String employeeName;
}
