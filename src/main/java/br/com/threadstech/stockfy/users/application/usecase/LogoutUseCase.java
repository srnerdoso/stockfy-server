package br.com.threadstech.stockfy.users.application.usecase;

import br.com.threadstech.stockfy.users.infrastructure.security.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LogoutUseCase {

	private final TokenService tokenService;

	public void execute(String refreshToken) {
		tokenService.revokeRefreshToken(refreshToken);
	}

}
