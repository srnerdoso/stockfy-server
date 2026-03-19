package br.com.threadstech.stockfy.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.RevisionEntity;
import org.hibernate.envers.RevisionNumber;
import org.hibernate.envers.RevisionTimestamp;

@Entity
@Getter
@Setter
@RevisionEntity(AuditRevisionListener.class)
@Table(name = "revinfo")
public class AuditRevisionEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @RevisionNumber
  private int id;

  @RevisionTimestamp
  private long timestamp;

  @Column(name = "username")
  private String username;
}
