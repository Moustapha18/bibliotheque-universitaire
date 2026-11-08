package com.exam.notifications.internal.service;

import com.exam.notifications.api.NotificationApi;
import com.exam.notifications.api.NotificationDto;
import com.exam.notifications.api.NotificationType;
import com.exam.notifications.internal.domain.Notification;
import com.exam.notifications.internal.domain.NotificationRepository;
import com.exam.notifications.internal.mapper.NotificationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;

/** Enregistre les notifications (aucun envoi d'e-mail réel n'est exigé par le sujet). */
@Service
@RequiredArgsConstructor
@Transactional
public class NotificationService implements NotificationApi {

    private final NotificationRepository repository;
    private final NotificationMapper mapper;
    private final Clock clock;

    public void enregistrer(Long adherentId, NotificationType type, String message) {
        repository.save(Notification.creer(adherentId, type, message, Instant.now(clock)));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NotificationDto> rechercher(Long adherentId, Pageable pageable) {
        Page<Notification> page = adherentId == null
                ? repository.findAll(pageable)
                : repository.findByAdherentId(adherentId, pageable);
        return page.map(mapper::toDto);
    }
}
