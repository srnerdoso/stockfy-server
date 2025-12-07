package br.com.threadstech.stockfy.service;

import br.com.threadstech.stockfy.entity.Customer;
import br.com.threadstech.stockfy.entity.Employee;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ConstraintNameService {

  private final MessageSource messageSource;

  public String getConstraintDisplayName(Class<?> entityClass, String constraintName) {
    String entityName = entityClass.getSimpleName().toLowerCase();
    return messageSource.getMessage(
        "constraint." + entityName + "." + constraintName,
        new Object[] {},
        LocaleContextHolder.getLocale());
  }
}
