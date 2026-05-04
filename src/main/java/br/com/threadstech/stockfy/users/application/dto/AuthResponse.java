package br.com.threadstech.stockfy.users.application.dto;

public record AuthResponse(String accessToken, String refreshToken) {
}
