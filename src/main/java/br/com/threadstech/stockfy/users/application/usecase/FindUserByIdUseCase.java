package br.com.threadstech.stockfy.users.application.usecase;

import br.com.threadstech.stockfy.users.application.dto.UserListItemResponse;
import br.com.threadstech.stockfy.users.application.dto.UserListType;
import br.com.threadstech.stockfy.users.application.exception.UserNotFoundException;
import br.com.threadstech.stockfy.users.domain.model.User;
import br.com.threadstech.stockfy.users.domain.repository.UserRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class FindUserByIdUseCase {

  private final UserRepository userRepository;

  @Transactional(readOnly = true)
  public UserListItemResponse execute(UUID requestedUserId) {
    User user = userRepository.findById(requestedUserId).orElseThrow(UserNotFoundException::new);
    return UserListItemResponse.from(user, UserListType.DETAILED);
  }
}
