package br.com.threadstech.stockfy.modules.users.application.usecase;

import br.com.threadstech.stockfy.modules.users.domain.model.*;
import br.com.threadstech.stockfy.modules.users.domain.repository.UserRepository;
import br.com.threadstech.stockfy.modules.users.infrastructure.security.JwtService;
import br.com.threadstech.stockfy.modules.users.infrastructure.security.TokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;
    @Mock
    private TokenService tokenService;

    @InjectMocks
    private LoginUseCase loginUseCase;
    @InjectMocks
    private RefreshTokenUseCase refreshTokenUseCase;
    @InjectMocks
    private LogoutUseCase logoutUseCase;

    private User user;
    private final UUID userId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(userId)
                .name("John Doe")
                .email(new Email("john@example.com"))
                .password(new Password("hashed_password"))
                .role(UserRole.USER)
                .status(UserStatus.ACTIVE)
                .active(true)
                .build();
    }

    @Test
    @DisplayName("Should login successfully")
    void shouldLoginSuccessfully() {
        when(userRepository.findByEmail(any())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(any(), any())).thenReturn(true);
        when(jwtService.generateToken(any(), any())).thenReturn("access_token");
        when(tokenService.generateRefreshToken(any())).thenReturn("refresh_token");

        var response = loginUseCase.execute("john@example.com", "password123");

        assertNotNull(response);
        assertEquals("access_token", response.accessToken());
        assertEquals("refresh_token", response.refreshToken());
    }

    @Test
    @DisplayName("Should throw exception for invalid credentials")
    void shouldThrowExceptionForInvalidCredentials() {
        when(userRepository.findByEmail(any())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(any(), any())).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> loginUseCase.execute("john@example.com", "wrong"));
    }

    @Test
    @DisplayName("Should refresh token successfully")
    void shouldRefreshTokenSuccessfully() {
        when(tokenService.validateRefreshToken(any())).thenReturn(true);
        when(tokenService.getUserIdFromRefreshToken(any())).thenReturn(userId);
        when(userRepository.findById(any())).thenReturn(Optional.of(user));
        when(jwtService.generateToken(any(), any())).thenReturn("new_access_token");

        var response = refreshTokenUseCase.execute("old_refresh_token");

        assertEquals("new_access_token", response.accessToken());
    }

    @Test
    @DisplayName("Should logout and revoke token")
    void shouldLogoutSuccessfully() {
        logoutUseCase.execute("refresh_token");
        verify(tokenService).revokeRefreshToken("refresh_token");
    }
}
