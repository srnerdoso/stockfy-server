package br.com.threadstech.stockfy.modules.users.application.dto;

public record AuthResponse(String accessToken, String refreshToken) {
}
