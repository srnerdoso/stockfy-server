package br.com.threadstech.stockfy.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "payment-service")
public class PaymentPropertiesConfig {
  private Integer refundMaxDays;
}
