package br.com.threadstech.stockfy.service;

import br.com.threadstech.stockfy.entity.AuditLog;
import br.com.threadstech.stockfy.enums.AuditI18nKeys;
import br.com.threadstech.stockfy.repository.AuditLogRepository;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuditLogService {

  private final AuditLogRepository auditLogRepository;

  @Transactional
  public void log(AuditI18nKeys action) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    String employeeName = authentication != null ? authentication.getName() : "System";

    AuditLog log = AuditLog.builder()
        .action(action)
        .timestamp(LocalDateTime.now())
        .employeeName(employeeName)
        .build();

    auditLogRepository.save(log);
  }
}
