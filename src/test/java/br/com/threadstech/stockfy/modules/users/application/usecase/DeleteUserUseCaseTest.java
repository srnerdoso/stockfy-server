/*
 * Copyright 2026 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package br.com.threadstech.stockfy.modules.users.application.usecase;

import java.util.UUID;

import br.com.threadstech.stockfy.users.application.usecase.DeleteUserUseCase;
import br.com.threadstech.stockfy.users.domain.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.verify;

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
