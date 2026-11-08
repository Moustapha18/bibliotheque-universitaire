package com.exam.transactions;

import com.exam.acteurs.api.AdherentApi;
import com.exam.acteurs.api.AdherentInfo;
import com.exam.acteurs.api.TypeAdherent;
import com.exam.catalogue.api.CatalogueApi;
import com.exam.shared.error.ConflictException;
import com.exam.shared.error.ResourceNotFoundException;
import com.exam.transactions.api.ReservationDisponible;
import com.exam.transactions.internal.domain.Reservation;
import com.exam.transactions.internal.domain.ReservationRepository;
import com.exam.transactions.internal.domain.StatutReservation;
import com.exam.transactions.internal.dto.ReservationRequest;
import com.exam.transactions.internal.mapper.ReservationMapper;
import com.exam.transactions.internal.service.ReservationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    private static final long ADHERENT_ID = 1L;
    private static final long OUVRAGE_ID = 100L;
    private static final long EXEMPLAIRE_ID = 10L;

    @Mock private ReservationRepository repository;
    @Mock private CatalogueApi catalogueApi;
    @Mock private AdherentApi adherentApi;
    @Mock private ApplicationEventPublisher eventPublisher;

    private ReservationService service;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(Instant.parse("2026-11-07T10:00:00Z"), ZoneOffset.UTC);
        service = new ReservationService(repository, catalogueApi, adherentApi, eventPublisher,
                Mappers.getMapper(ReservationMapper.class), clock);
    }

    @Test
    void refusesToReserveWhenACopyIsAvailable() {
        givenActiveAdherent();
        when(catalogueApi.ouvrageExiste(OUVRAGE_ID)).thenReturn(true);
        when(catalogueApi.compterExemplairesDisponibles(OUVRAGE_ID)).thenReturn(1L);

        assertThatThrownBy(() -> service.reserver(new ReservationRequest(OUVRAGE_ID, ADHERENT_ID)))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("disponibles");
        verify(repository, never()).save(any());
    }

    @Test
    void refusesToReserveAnUnknownBook() {
        givenActiveAdherent();
        when(catalogueApi.ouvrageExiste(OUVRAGE_ID)).thenReturn(false);

        assertThatThrownBy(() -> service.reserver(new ReservationRequest(OUVRAGE_ID, ADHERENT_ID)))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void refusesADuplicateActiveReservation() {
        givenActiveAdherent();
        when(catalogueApi.ouvrageExiste(OUVRAGE_ID)).thenReturn(true);
        when(catalogueApi.compterExemplairesDisponibles(OUVRAGE_ID)).thenReturn(0L);
        when(repository.existsByOuvrageIdAndAdherentIdAndStatutIn(anyLong(), anyLong(), anyCollection())).thenReturn(true);

        assertThatThrownBy(() -> service.reserver(new ReservationRequest(OUVRAGE_ID, ADHERENT_ID)))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void queuesAReservationWhenNoCopyIsAvailable() {
        givenActiveAdherent();
        when(catalogueApi.ouvrageExiste(OUVRAGE_ID)).thenReturn(true);
        when(catalogueApi.compterExemplairesDisponibles(OUVRAGE_ID)).thenReturn(0L);
        when(repository.save(any(Reservation.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.reserver(new ReservationRequest(OUVRAGE_ID, ADHERENT_ID));

        assertThat(response.statut()).isEqualTo(StatutReservation.EN_ATTENTE);
    }

    @Test
    void returnedCopyGoesToTheHeadOfTheQueue() {
        Reservation first = Reservation.creer(OUVRAGE_ID, 7L, Instant.parse("2026-11-01T08:00:00Z"));
        when(repository.findFirstByOuvrageIdAndStatutOrderByDateReservationAscIdAsc(OUVRAGE_ID, StatutReservation.EN_ATTENTE))
                .thenReturn(Optional.of(first));

        service.redistribuerExemplaire(OUVRAGE_ID, EXEMPLAIRE_ID);

        assertThat(first.getStatut()).isEqualTo(StatutReservation.PRETE);
        assertThat(first.getExemplaireId()).isEqualTo(EXEMPLAIRE_ID);
        verify(catalogueApi).marquerReserve(EXEMPLAIRE_ID);
        verify(catalogueApi, never()).marquerDisponible(anyLong());
        verify(eventPublisher).publishEvent(any(ReservationDisponible.class));
    }

    @Test
    void returnedCopyBecomesAvailableWhenNobodyIsWaiting() {
        when(repository.findFirstByOuvrageIdAndStatutOrderByDateReservationAscIdAsc(OUVRAGE_ID, StatutReservation.EN_ATTENTE))
                .thenReturn(Optional.empty());

        service.redistribuerExemplaire(OUVRAGE_ID, EXEMPLAIRE_ID);

        verify(catalogueApi).marquerDisponible(EXEMPLAIRE_ID);
        verify(catalogueApi, never()).marquerReserve(anyLong());
    }

    @Test
    void reservedCopyCanOnlyBeBorrowedByTheHolder() {
        Reservation prete = Reservation.creer(OUVRAGE_ID, 7L, Instant.now());
        prete.marquerPrete(EXEMPLAIRE_ID);
        when(repository.findByExemplaireIdAndStatut(EXEMPLAIRE_ID, StatutReservation.PRETE)).thenReturn(Optional.of(prete));

        assertThatThrownBy(() -> service.verifierDetenteur(EXEMPLAIRE_ID, 8L)).isInstanceOf(ConflictException.class);
        service.verifierDetenteur(EXEMPLAIRE_ID, 7L);
    }

    private void givenActiveAdherent() {
        when(adherentApi.getAdherent(ADHERENT_ID))
                .thenReturn(new AdherentInfo(ADHERENT_ID, "Alice Martin", TypeAdherent.ETUDIANT, 3, true));
    }
}
