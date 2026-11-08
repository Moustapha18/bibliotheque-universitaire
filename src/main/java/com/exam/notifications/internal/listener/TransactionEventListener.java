package com.exam.notifications.internal.listener;

import com.exam.notifications.api.NotificationType;
import com.exam.notifications.internal.service.NotificationService;
import com.exam.transactions.api.EmpruntEffectue;
import com.exam.transactions.api.ReservationDisponible;
import com.exam.transactions.api.RetourEffectue;
import lombok.RequiredArgsConstructor;
import org.springframework.modulith.ApplicationModuleListener;
import org.springframework.stereotype.Component;

/**
 * Consomme, de façon asynchrone et après commit, les événements publiés par le module transactions.
 * Le module transactions ne connaît pas ce listener : couplage uniquement par événements.
 */
@Component
@RequiredArgsConstructor
public class TransactionEventListener {

    private final NotificationService notificationService;

    @ApplicationModuleListener
    public void on(EmpruntEffectue event) {
        notificationService.enregistrer(event.adherentId(), NotificationType.RAPPEL_RETOUR,
                "Exemplaire %d emprunté : à rendre avant le %s.".formatted(event.exemplaireId(), event.dateRetourPrevue()));
    }

    @ApplicationModuleListener
    public void on(RetourEffectue event) {
        if (event.joursRetard() > 0) {
            notificationService.enregistrer(event.adherentId(), NotificationType.ALERTE_RETARD,
                    "Exemplaire %d rendu avec %d jour(s) de retard : pénalité de %s €."
                            .formatted(event.exemplaireId(), event.joursRetard(), event.penalite()));
            return;
        }
        notificationService.enregistrer(event.adherentId(), NotificationType.CONFIRMATION_RETOUR,
                "Retour de l'exemplaire %d enregistré. Merci !".formatted(event.exemplaireId()));
    }

    @ApplicationModuleListener
    public void on(ReservationDisponible event) {
        notificationService.enregistrer(event.adherentId(), NotificationType.RESERVATION_DISPONIBLE,
                "Votre réservation %d est prête : l'exemplaire %d vous attend."
                        .formatted(event.reservationId(), event.exemplaireId()));
    }
}
