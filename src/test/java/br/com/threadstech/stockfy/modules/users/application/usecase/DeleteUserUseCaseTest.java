package br.com.threadstech.stockfy.modules.users.application.usecase;

import static org.mockito.Mockito.verify;

import br.com.threadstech.stockfy.modules.users.domain.repository.UserRepository;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DeleteUserUseCaseTest {

  private final UserRepository userRepository = org.mockito.Mockito.mock(UserRepository.class);
  private final DeleteUserUseCase useCase = new DeleteUserUseCase(userRepository);

  @Test
  @DisplayName("Deve delegar exclusao para o repositorio quando ID for informado")
  void execute_whenUserIdIsProvided_thenDelegatesDeleteById() {
    UUID userId = UUID.randomUUID();

    useCase.execute(userId);

    verify(userRepository).deleteById(userId);
  }
}
