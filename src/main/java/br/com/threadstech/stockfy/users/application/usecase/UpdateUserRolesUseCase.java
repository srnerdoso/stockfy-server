package br.com.threadstech.stockfy.users.application.usecase;

import br.com.threadstech.stockfy.users.application.exception.UserNotFoundException;
import br.com.threadstech.stockfy.users.domain.model.UserRole;
import br.com.threadstech.stockfy.users.domain.repository.UserRepository;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class UpdateUserRolesUseCase {

	private final UserRepository userRepository;

	@Transactional
	public void execute(UUID userId, Set<UserRole> rolesToAdd, Set<UserRole> rolesToRemove) {
		var user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
		user.updateRoles(rolesToAdd, rolesToRemove);
		userRepository.update(user);
	}

}
