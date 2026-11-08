package com.exam.notifications.api;

import java.time.Instant;

public record NotificationDto(Long id, Long adherentId, NotificationType type, String message, Instant createdAt) {
}
