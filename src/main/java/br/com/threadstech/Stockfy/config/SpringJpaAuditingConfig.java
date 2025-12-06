package br.com.threadstech.stockfy.config;

import java.util.Optional;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration
@EnableJpaAuditing
public class SpringJpaAuditingConfig implements AuditorAware<String> {

  @Override
  public Optional<String> getCurrentAuditor() {
    // TODO: Implementar auditoria quando JWT for implementado
    return Optional.of("system");
  }
}
