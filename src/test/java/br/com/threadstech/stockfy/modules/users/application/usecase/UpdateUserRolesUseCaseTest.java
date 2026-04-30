package br.com.threadstech.stockfy.modules.users.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.threadstech.stockfy.modules.users.application.exception.UserNotFoundException;
import br.com.threadstech.stockfy.modules.users.domain.exception.InvalidUserRolesException;
import br.com.threadstech.stockfy.modules.users.domain.model.User;
import br.com.threadstech.stockfy.modules.users.domain.model.UserRole;
import br.com.threadstech.stockfy.modules.users.domain.repository.UserRepository;
import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UpdateUserRolesUseCaseTest {

  @Mock private UserRepository userRepository;

  @InjectMocks private UpdateUserRolesUseCase useCase;

  private UUID userId;
  private User user;

  @BeforeEach
  void setUp() {
    userId = UUID.randomUUID();
    user = User.builder().id(userId).roles(EnumSet.of(UserRole.USER)).build();
  }

  @Test
  @DisplayName("Deve adicionar role ADMIN a um usuario que possui apenas USER")
  void execute_whenAddingAdminToUser_thenAddsRole() {
    when(userRepository.findById(userId)).thenReturn(Optional.of(user));

    useCase.execute(userId, Set.of(UserRole.ADMIN), Set.of());

    assertEquals(Set.of(UserRole.USER, UserRole.ADMIN), user.getRoles());
    verify(userRepository).update(user);
  }

  @Test
  @DisplayName("Deve remover role USER de um usuario que possui {USER, ADMIN}")
  void execute_whenRemovingUserFromMultiRoleUser_thenRemovesRole() {
    user.setRoles(Set.of(UserRole.USER, UserRole.ADMIN));
    when(userRepository.findById(userId)).thenReturn(Optional.of(user));

    useCase.execute(userId, Set.of(), Set.of(UserRole.USER));

    assertEquals(Set.of(UserRole.ADMIN), user.getRoles());
    verify(userRepository).update(user);
  }

  @Test
  @DisplayName("Nao deve alterar roles quando add e remove forem vazios")
  void execute_whenEmptyRoles_thenNoChange() {
    when(userRepository.findById(userId)).thenReturn(Optional.of(user));

    useCase.execute(userId, Set.of(), Set.of());

    assertEquals(Set.of(UserRole.USER), user.getRoles());
    verify(userRepository).update(user);
  }

  @Test
  @DisplayName("Deve lancar excecao ao tentar remover todas as roles")
  void execute_whenRemovingAllRoles_thenThrowsException() {
    when(userRepository.findById(userId)).thenReturn(Optional.of(user));

    assertThrows(
        InvalidUserRolesException.class, () -> useCase.execute(userId, Set.of(), Set.of(UserRole.USER)));
  }

  @Test
  @DisplayName("Deve lancar excecao quando usuario nao for encontrado")
  void execute_whenUserNotFound_thenThrowsException() {
    when(userRepository.findById(userId)).thenReturn(Optional.empty());

    assertThrows(
        UserNotFoundException.class, () -> useCase.execute(userId, Set.of(UserRole.ADMIN), Set.of()));
  }
}
