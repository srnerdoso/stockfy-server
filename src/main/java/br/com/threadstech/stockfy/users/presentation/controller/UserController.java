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

package br.com.threadstech.stockfy.users.presentation.controller;

import java.util.UUID;

import br.com.threadstech.stockfy.users.application.dto.FindAllUsersResponse;
import br.com.threadstech.stockfy.users.application.dto.RegisterUserRequest;
import br.com.threadstech.stockfy.users.application.dto.UpdateCurrentUserRequest;
import br.com.threadstech.stockfy.users.application.dto.UpdatePasswordRequest;
import br.com.threadstech.stockfy.users.application.dto.UpdateUserRolesRequest;
import br.com.threadstech.stockfy.users.application.dto.UserListItemResponse;
import br.com.threadstech.stockfy.users.application.dto.UserListType;
import br.com.threadstech.stockfy.users.application.dto.UserResponse;
import br.com.threadstech.stockfy.users.application.usecase.DeleteUserUseCase;
import br.com.threadstech.stockfy.users.application.usecase.FindAllUsersUseCase;
import br.com.threadstech.stockfy.users.application.usecase.FindUserByIdUseCase;
import br.com.threadstech.stockfy.users.application.usecase.GenerateResetCodeUseCase;
import br.com.threadstech.stockfy.users.application.usecase.RegisterUserUseCase;
import br.com.threadstech.stockfy.users.application.usecase.UnlockUserUseCase;
import br.com.threadstech.stockfy.users.application.usecase.UpdatePasswordUseCase;
import br.com.threadstech.stockfy.users.application.usecase.UpdateProfileUseCase;
import br.com.threadstech.stockfy.users.application.usecase.UpdateUserRolesUseCase;
import br.com.threadstech.stockfy.users.domain.repository.UserRepository;
import br.com.threadstech.stockfy.users.presentation.mapper.UserResponseMapperFactory;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Validated
public class UserController {

	private final RegisterUserUseCase registerUserUseCase;

	private final FindAllUsersUseCase findAllUsersUseCase;

	private final FindUserByIdUseCase findUserByIdUseCase;

	private final UpdateProfileUseCase updateProfileUseCase;

	private final DeleteUserUseCase deleteUserUseCase;

	private final GenerateResetCodeUseCase generateResetCodeUseCase;

	private final UpdatePasswordUseCase updatePasswordUseCase;

	private final UnlockUserUseCase unlockUserUseCase;

	private final UpdateUserRolesUseCase updateUserRolesUseCase;

	private final UserRepository userRepository;

	private final UserResponseMapperFactory mapperFactory;

	@PostMapping
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<Void> register(@RequestBody @Valid RegisterUserRequest request) {
		registerUserUseCase.execute(request);
		return ResponseEntity.status(HttpStatus.CREATED).build();
	}

	@PatchMapping("/{id}/roles")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<Void> updateUserRoles(@PathVariable UUID id,
			@RequestBody @Valid UpdateUserRolesRequest request) {
		updateUserRolesUseCase.execute(id, request.rolesToAdd(), request.rolesToRemove());
		return ResponseEntity.noContent().build();
	}

	@PostMapping("/{id}/password-reset-codes")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<ResetCodeResponse> generateResetCode(@PathVariable UUID id) {
		String code = generateResetCodeUseCase.execute(id);
		return ResponseEntity.ok(new ResetCodeResponse(code));
	}

	@PreAuthorize("#request.code() != null or !isAnonymous()")
	@PatchMapping("/password")
	public ResponseEntity<Void> updatePassword(@RequestBody @Valid UpdatePasswordRequest request,
			Authentication authentication) {
		if (request.code() != null) {
			updatePasswordUseCase.executeWithCode(request.code(), request.newPassword(), request.confirmPassword());
			return ResponseEntity.noContent().build();
		}

		UUID userId = (UUID) authentication.getPrincipal();
		updatePasswordUseCase.executeAuthenticated(userId, request.currentPassword(), request.newPassword(),
				request.confirmPassword());
		return ResponseEntity.noContent().build();
	}

	@GetMapping("/me")
	public ResponseEntity<UserResponse> me(Authentication authentication) {
		UUID userId = (UUID) authentication.getPrincipal();
		return userRepository.findById(userId)
			.map(mapperFactory::toResponse)
			.map(ResponseEntity::ok)
			.orElse(ResponseEntity.notFound().build());
	}

	@GetMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN') or #id == authentication.principal")
	public ResponseEntity<UserListItemResponse> getById(@PathVariable UUID id) {
		return ResponseEntity.ok(findUserByIdUseCase.execute(id));
	}

	@GetMapping
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<FindAllUsersResponse<UserListItemResponse>> findAll(
			@RequestParam(required = false) @Size(max = 255, message = "{user.name.size}") @Pattern(
					regexp = "^[\\p{L}\\p{M}0-9 .'-]+$", message = "{user.name.pattern}") String name,
			@RequestParam UserListType type, @PageableDefault(size = 20) Pageable pageable) {
		return ResponseEntity.ok(findAllUsersUseCase.execute(name, type, pageable));
	}

	@PatchMapping("/me")
	public ResponseEntity<Void> updateProfile(@RequestBody @Valid UpdateCurrentUserRequest request,
			Authentication authentication) {
		UUID userId = (UUID) authentication.getPrincipal();
		updateProfileUseCase.execute(userId, request.name(), request.email());
		return ResponseEntity.noContent().build();
	}

	@DeleteMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<Void> delete(@PathVariable UUID id) {
		deleteUserUseCase.execute(id);
		return ResponseEntity.noContent().build();
	}

	@PatchMapping("/{id}/unlock")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<Void> unlock(@PathVariable UUID id) {
		unlockUserUseCase.execute(id);
		return ResponseEntity.noContent().build();
	}

	public record ResetCodeResponse(String code) {
	}

}
