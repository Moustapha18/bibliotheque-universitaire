package com.exam.notifications.api;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/** Contrat public du module notifications (consultation). */
public interface NotificationApi {

    /** @param adherentId filtre optionnel ; null = toutes les notifications */
    Page<NotificationDto> rechercher(Long adherentId, Pageable pageable);
}
