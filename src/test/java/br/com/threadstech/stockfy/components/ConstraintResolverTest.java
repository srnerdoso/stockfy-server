package br.com.threadstech.stockfy.components;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

import br.com.threadstech.stockfy.config.constraints.CustomerConstraintNames;
import br.com.threadstech.stockfy.entity.Customer;
import br.com.threadstech.stockfy.service.ConstraintNameService;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

@ExtendWith(MockitoExtension.class)
class ConstraintResolverTest {

  @Mock private ConstraintNameService constraintNameService;

  @InjectMocks private ConstraintResolver constraintResolver;

  @Test
  @DisplayName("Should resolve display name correctly when ConstraintViolationException is present")
  void shouldResolveDisplayNameCorrectly() {
    String rawConstraint = CustomerConstraintNames.UK_CPF;
    ConstraintViolationException cve = mock(ConstraintViolationException.class);
    when(cve.getConstraintName()).thenReturn(rawConstraint);

    DataIntegrityViolationException dive = new DataIntegrityViolationException("error", cve);

    when(constraintNameService.getConstraintDisplayName(Customer.class, "cpf"))
        .thenReturn("CPF already exists");

    String result = constraintResolver.resolveDisplayName(dive, CustomerConstraintNames.class);

    assertThat(result).isEqualTo("CPF already exists");
    verify(constraintNameService).getConstraintDisplayName(Customer.class, "cpf");
  }

  @Test
  @DisplayName("Should return null when cause is not a ConstraintViolationException")
  void shouldReturnNullWhenNotConstraintViolation() {
    DataIntegrityViolationException dive =
        new DataIntegrityViolationException("error", new RuntimeException());

    String result = constraintResolver.resolveDisplayName(dive, CustomerConstraintNames.class);

    assertThat(result).isNull();
    verifyNoInteractions(constraintNameService);
  }

  @Test
  @DisplayName("Should throw IllegalArgumentException when reflection fails")
  void shouldThrowExceptionWhenReflectionFails() {
    Exception ex = new Exception();
    assertThatThrownBy(() -> constraintResolver.resolveDisplayName(ex, null))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  @DisplayName("Should handle unmapped constraint name by returning null from service or null itself")
  void shouldHandleUnmappedConstraint() {
    String unknownConstraint = "uk_unknown";
    ConstraintViolationException cve = mock(ConstraintViolationException.class);
    when(cve.getConstraintName()).thenReturn(unknownConstraint);

    DataIntegrityViolationException dive = new DataIntegrityViolationException("error", cve);

    // CustomerConstraintNames.getI18nKeyMap("uk_unknown") will return null
    String result = constraintResolver.resolveDisplayName(dive, CustomerConstraintNames.class);

    assertThat(result).isNull();
    verify(constraintNameService).getConstraintDisplayName(Customer.class, null);
  }
}
