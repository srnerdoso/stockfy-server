package br.com.threadstech.stockfy.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Getter
@Setter
@MappedSuperclass
// @EntityListeners(AuditingEntityListener.class)
public class AuditListener {

  // TODO: Remover default values quando o AuditorAware for configurado

  @CreatedDate
  @Column(name = "created_at", nullable = false)
  private Instant createdAt = Instant.now();

  @LastModifiedDate
  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt = Instant.now();

  @CreatedBy
  @Column(name = "created_by", nullable = false)
  private String createdBy = "srnerdoso";

  @LastModifiedBy
  @Column(name = "updated_by", nullable = false)
  private String updatedBy = "srnerdoso";
}
