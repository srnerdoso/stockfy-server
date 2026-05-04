package br.com.threadstech.stockfy.users.application.usecase;

import br.com.threadstech.stockfy.users.domain.repository.UserRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class DeleteUserUseCase {

	private final UserRepository userRepository;

	@Transactional
	public void execute(UUID userId) {
		userRepository.deleteById(userId);
	}

}
