package br.com.threadstech.stockfy.repository;

import br.com.threadstech.stockfy.entity.AuditLog;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

  @Query("SELECT a FROM AuditLog a ORDER BY a.timestamp DESC")
  List<AuditLog> findLatest(Pageable pageable);
}
