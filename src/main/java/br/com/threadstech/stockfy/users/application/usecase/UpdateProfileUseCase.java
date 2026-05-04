package br.com.threadstech.stockfy.users.application.usecase;

import br.com.threadstech.stockfy.users.application.exception.EmailAlreadyExistsException;
import br.com.threadstech.stockfy.users.application.exception.UserNotFoundException;
import br.com.threadstech.stockfy.users.domain.model.Email;
import br.com.threadstech.stockfy.users.domain.model.User;
import br.com.threadstech.stockfy.users.domain.repository.UserRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class UpdateProfileUseCase {

	private final UserRepository userRepository;

	@Transactional
	public void execute(UUID userId, String name, String email) {
		User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
		Email newEmail = resolveNewEmail(user, email);

		if (name == null && newEmail == null) {
			return;
		}

		if (name != null) {
			user.setName(name);
		}

		if (newEmail != null) {
			user.setEmail(newEmail);
		}

		userRepository.update(user);
	}

	private Email resolveNewEmail(User user, String email) {
		if (email == null) {
			return null;
		}

		Email newEmail = new Email(email);
		if (newEmail.equals(user.getEmail())) {
			return null;
		}

		userRepository.findByEmail(newEmail)
			.filter(existingUser -> !existingUser.getId().equals(user.getId()))
			.ifPresent(existingUser -> {
				throw new EmailAlreadyExistsException();
			});

		return newEmail;
	}

}
