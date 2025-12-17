package br.com.threadstech.stockfy.components;

import br.com.threadstech.stockfy.config.constraints.ConstraintNames;
import br.com.threadstech.stockfy.config.constraints.CustomerConstraintNames;
import br.com.threadstech.stockfy.exception.CustomerUniqueViolationException;
import br.com.threadstech.stockfy.service.ConstraintNameService;
import java.lang.reflect.Constructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ConstraintResolver {

  private final ConstraintNameService constraintNameService;

  public String resolveDisplayName(
      Exception ex, Class<? extends ConstraintNames<?>> constraintNamesClass) {
    try {
      Constructor<? extends ConstraintNames<?>> constraintNames =
          constraintNamesClass.getDeclaredConstructor();
      ConstraintNames<?> instance = constraintNames.newInstance();

      if (ex.getCause() instanceof org.hibernate.exception.ConstraintViolationException cve) {
        log.info("Constraint Error From: {}", cve.getConstraintName());

        String constraintName = instance.getI18nKeyMap(cve.getConstraintName());
        Class<?> entityClass = instance.getI18nEntityClass();

        return constraintNameService.getConstraintDisplayName(entityClass, constraintName);
      }
      return null;
    } catch (Exception e) {
      log.error("Unmapped constraint from exception: {}", e.getMessage());
      throw new IllegalArgumentException(e);
    }
  }
}
