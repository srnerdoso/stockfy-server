package br.com.threadstech.stockfy.users.application.usecase;

import br.com.threadstech.stockfy.users.application.exception.UserNotFoundException;
import br.com.threadstech.stockfy.users.application.port.ResetCodeHasher;
import br.com.threadstech.stockfy.users.domain.model.User;
import br.com.threadstech.stockfy.users.domain.repository.UserRepository;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GenerateResetCodeUseCase {

	private final UserRepository userRepository;

	private final ResetCodeHasher resetCodeHasher;

	@Transactional
	public String execute(UUID userId) {
		User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);

		String code = String.format("%06d", new SecureRandom().nextInt(1000000));
		user.setResetPasswordCodeHash(resetCodeHasher.hash(code));
		user.setResetPasswordExpiresAt(LocalDateTime.now().plusHours(24));

		userRepository.update(user);
		return code;
	}

}
