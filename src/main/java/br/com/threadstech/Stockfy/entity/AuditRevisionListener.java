package br.com.threadstech.stockfy.entity;

import org.hibernate.envers.RevisionListener;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class AuditRevisionListener implements RevisionListener {

  @Override
  public void newRevision(Object revisionEntity) {
    AuditRevisionEntity entity = (AuditRevisionEntity) revisionEntity;
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    entity.setUsername(authentication != null ? authentication.getName() : "System");
  }
}
