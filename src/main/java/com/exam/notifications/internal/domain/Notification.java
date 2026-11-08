package com.exam.notifications.internal.domain;

import com.exam.notifications.api.NotificationType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "notification")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long adherentId;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_notification", nullable = false, length = 30)
    private NotificationType type;

    @Column(nullable = false, length = 500)
    private String message;

    @Column(nullable = false)
    private Instant createdAt;

    public static Notification creer(Long adherentId, NotificationType type, String message, Instant createdAt) {
        Notification notification = new Notification();
        notification.adherentId = adherentId;
        notification.type = type;
        notification.message = message;
        notification.createdAt = createdAt;
        return notification;
    }
}
