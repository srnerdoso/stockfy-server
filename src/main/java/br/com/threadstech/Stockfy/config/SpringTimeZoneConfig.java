package br.com.threadstech.stockfy.config;

import jakarta.annotation.PostConstruct;
import java.util.TimeZone;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SpringTimeZoneConfig {

  @PostConstruct
  public void timeZoneConfig() {
    TimeZone.setDefault(TimeZone.getTimeZone("America/Sao_Paulo"));
  }
}
