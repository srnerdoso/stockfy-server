package br.com.threadstech.stockfy.modules.users.domain.event;

import java.util.UUID;

public record AccountLockedEvent(UUID userId, String email, String reason) {
}
