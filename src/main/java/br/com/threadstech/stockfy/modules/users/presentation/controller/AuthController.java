package br.com.threadstech.stockfy.modules.users.presentation.controller;

import br.com.threadstech.stockfy.modules.users.application.dto.AuthResponse;
import br.com.threadstech.stockfy.modules.users.application.usecase.LoginUseCase;
import br.com.threadstech.stockfy.modules.users.application.usecase.LogoutUseCase;
import br.com.threadstech.stockfy.modules.users.application.usecase.RefreshTokenUseCase;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth/sessions")
@RequiredArgsConstructor
public class AuthController {

    private final LoginUseCase loginUseCase;
    private final RefreshTokenUseCase refreshTokenUseCase;
    private final LogoutUseCase logoutUseCase;

    @PostMapping
    public ResponseEntity<Void> login(@RequestBody LoginRequest request, HttpServletResponse response) {
        AuthResponse authResponse = loginUseCase.execute(request.email(), request.password());
        addCookies(response, authResponse);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/refresh")
    public ResponseEntity<Void> refresh(@CookieValue(name = "refresh_token") String refreshToken, HttpServletResponse response) {
        AuthResponse authResponse = refreshTokenUseCase.execute(refreshToken);
        addCookies(response, authResponse);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/current")
    public ResponseEntity<Void> logout(@CookieValue(name = "refresh_token") String refreshToken, HttpServletResponse response) {
        logoutUseCase.execute(refreshToken);
        clearCookies(response);
        return ResponseEntity.noContent().build();
    }

    private void addCookies(HttpServletResponse response, AuthResponse authResponse) {
        Cookie accessCookie = new Cookie("access_token", authResponse.accessToken());
        accessCookie.setHttpOnly(true);
        accessCookie.setSecure(true); // Should be true in prod
        accessCookie.setPath("/");
        accessCookie.setMaxAge(900); // 15 min

        Cookie refreshCookie = new Cookie("refresh_token", authResponse.refreshToken());
        refreshCookie.setHttpOnly(true);
        refreshCookie.setSecure(true);
        refreshCookie.setPath("/");
        refreshCookie.setMaxAge(604800); // 7 days

        response.addCookie(accessCookie);
        response.addCookie(refreshCookie);
    }

    private void clearCookies(HttpServletResponse response) {
        Cookie accessCookie = new Cookie("access_token", null);
        accessCookie.setPath("/");
        accessCookie.setMaxAge(0);
        Cookie refreshCookie = new Cookie("refresh_token", null);
        refreshCookie.setPath("/");
        refreshCookie.setMaxAge(0);
        response.addCookie(accessCookie);
        response.addCookie(refreshCookie);
    }

    public record LoginRequest(String email, String password) {}
}
