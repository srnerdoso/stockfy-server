package br.com.threadstech.stockfy.modules.users.application.usecase;

import br.com.threadstech.stockfy.modules.users.infrastructure.security.TokenService;
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
