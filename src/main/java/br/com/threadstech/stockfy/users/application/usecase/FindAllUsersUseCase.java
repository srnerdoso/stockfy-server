package br.com.threadstech.stockfy.users.application.usecase;

import br.com.threadstech.stockfy.users.application.dto.FindAllUsersResponse;
import br.com.threadstech.stockfy.users.application.dto.UserListItemResponse;
import br.com.threadstech.stockfy.users.application.dto.UserListType;
import br.com.threadstech.stockfy.users.domain.model.User;
import br.com.threadstech.stockfy.users.domain.repository.UserRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class FindAllUsersUseCase {

	private final UserRepository userRepository;

	@Transactional(readOnly = true)
	public FindAllUsersResponse<UserListItemResponse> execute(String name, UserListType type, Pageable pageable) {
		Page<User> users = userRepository.findAll(normalizeName(name), pageable);
		List<UserListItemResponse> content = users.getContent()
			.stream()
			.map(user -> UserListItemResponse.from(user, type))
			.toList();

		return new FindAllUsersResponse<>(content, users.getNumber(), users.getSize(), users.getTotalElements(),
				users.getTotalPages());
	}

	private String normalizeName(String name) {
		if (name == null || name.isBlank()) {
			return null;
		}
		return name.trim();
	}

}
