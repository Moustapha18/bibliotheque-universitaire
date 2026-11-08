package com.exam.transactions;

import com.exam.acteurs.api.AdherentApi;
import com.exam.acteurs.api.AdherentInfo;
import com.exam.acteurs.api.TypeAdherent;
import com.exam.catalogue.api.CatalogueApi;
import com.exam.catalogue.api.ExemplaireInfo;
import com.exam.catalogue.api.StatutExemplaire;
import com.exam.shared.error.ConflictException;
import com.exam.transactions.api.EmpruntEffectue;
import com.exam.transactions.api.RetourEffectue;
import com.exam.transactions.internal.config.LibraryProperties;
import com.exam.transactions.internal.domain.Emprunt;
import com.exam.transactions.internal.domain.EmpruntRepository;
import com.exam.transactions.internal.dto.EmpruntRequest;
import com.exam.transactions.internal.dto.EmpruntResponse;
import com.exam.transactions.internal.mapper.EmpruntMapper;
import com.exam.transactions.internal.service.EmpruntService;
import com.exam.transactions.internal.service.PenaltyCalculator;
import com.exam.transactions.internal.service.ReservationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmpruntServiceTest {

    private static final long ADHERENT_ID = 1L;
    private static final long EXEMPLAIRE_ID = 10L;
    private static final long OUVRAGE_ID = 100L;
    private static final LocalDate TODAY = LocalDate.of(2026, 11, 7);

    @Mock private EmpruntRepository empruntRepository;
    @Mock private ReservationService reservationService;
    @Mock private CatalogueApi catalogueApi;
    @Mock private AdherentApi adherentApi;
    @Mock private ApplicationEventPublisher eventPublisher;

    private EmpruntService service;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(Instant.parse("2026-11-07T10:00:00Z"), ZoneOffset.UTC);
        PenaltyCalculator calculator = new PenaltyCalculator(new LibraryProperties(14, new BigDecimal("0.50")));
        service = new EmpruntService(empruntRepository, reservationService, catalogueApi, adherentApi,
                calculator, eventPublisher, Mappers.getMapper(EmpruntMapper.class), clock);
    }

    @Test
    void refusesBorrowWhenStudentQuotaIsReached() {
        givenAdherent(TypeAdherent.ETUDIANT, true);
        when(empruntRepository.countByAdherentIdAndDateRetourEffectiveIsNull(ADHERENT_ID)).thenReturn(3L);

        assertThatThrownBy(() -> service.emprunter(new EmpruntRequest(ADHERENT_ID, EXEMPLAIRE_ID)))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Quota");
        verify(catalogueApi, never()).marquerEmprunte(any());
    }

    @Test
    void acceptsBorrowForTeacherWithFourLoans() {
        givenAdherent(TypeAdherent.ENSEIGNANT, true);
        when(empruntRepository.countByAdherentIdAndDateRetourEffectiveIsNull(ADHERENT_ID)).thenReturn(4L);
        givenExemplaire(StatutExemplaire.DISPONIBLE);
        when(empruntRepository.save(any(Emprunt.class))).thenAnswer(invocation -> invocation.getArgument(0));

        EmpruntResponse response = service.emprunter(new EmpruntRequest(ADHERENT_ID, EXEMPLAIRE_ID));

        assertThat(response.dateRetourPrevue()).isEqualTo(TODAY.plusDays(14));
        verify(catalogueApi).marquerEmprunte(EXEMPLAIRE_ID);
        verify(eventPublisher).publishEvent(any(EmpruntEffectue.class));
    }

    @Test
    void refusesBorrowOfAnAlreadyBorrowedCopy() {
        givenAdherent(TypeAdherent.ETUDIANT, true);
        when(empruntRepository.countByAdherentIdAndDateRetourEffectiveIsNull(ADHERENT_ID)).thenReturn(0L);
        givenExemplaire(StatutExemplaire.EMPRUNTE);

        assertThatThrownBy(() -> service.emprunter(new EmpruntRequest(ADHERENT_ID, EXEMPLAIRE_ID)))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("déjà emprunté");
        verify(catalogueApi, never()).marquerEmprunte(any());
    }

    @Test
    void checksTheReservationHolderWhenTheCopyIsReserved() {
        givenAdherent(TypeAdherent.ETUDIANT, true);
        when(empruntRepository.countByAdherentIdAndDateRetourEffectiveIsNull(ADHERENT_ID)).thenReturn(0L);
        givenExemplaire(StatutExemplaire.RESERVE);
        org.mockito.Mockito.doThrow(new ConflictException("réservé pour un autre adhérent"))
                .when(reservationService).verifierDetenteur(EXEMPLAIRE_ID, ADHERENT_ID);

        assertThatThrownBy(() -> service.emprunter(new EmpruntRequest(ADHERENT_ID, EXEMPLAIRE_ID)))
                .isInstanceOf(ConflictException.class);
        verify(catalogueApi, never()).marquerEmprunte(any());
    }

    @Test
    void refusesBorrowForADeactivatedMember() {
        givenAdherent(TypeAdherent.ETUDIANT, false);

        assertThatThrownBy(() -> service.emprunter(new EmpruntRequest(ADHERENT_ID, EXEMPLAIRE_ID)))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("désactivé");
    }

    @Test
    void lateReturnComputesLateDaysAndPenaltyAndPublishesTheEvent() {
        Emprunt emprunt = Emprunt.ouvrir(ADHERENT_ID, EXEMPLAIRE_ID, TODAY.minusDays(20), TODAY.minusDays(6));
        when(empruntRepository.findById(5L)).thenReturn(Optional.of(emprunt));
        givenExemplaire(StatutExemplaire.EMPRUNTE);

        EmpruntResponse response = service.retourner(5L);

        assertThat(response.joursRetard()).isEqualTo(6);
        assertThat(response.penalite()).isEqualByComparingTo("3.00");
        assertThat(response.dateRetourEffective()).isEqualTo(TODAY);
        verify(reservationService).redistribuerExemplaire(OUVRAGE_ID, EXEMPLAIRE_ID);

        ArgumentCaptor<RetourEffectue> event = ArgumentCaptor.forClass(RetourEffectue.class);
        verify(eventPublisher).publishEvent(event.capture());
        assertThat(event.getValue().joursRetard()).isEqualTo(6);
    }

    @Test
    void refusesToReturnTwice() {
        Emprunt emprunt = Emprunt.ouvrir(ADHERENT_ID, EXEMPLAIRE_ID, TODAY.minusDays(3), TODAY.plusDays(11));
        emprunt.cloturer(TODAY, 0, BigDecimal.ZERO);
        when(empruntRepository.findById(5L)).thenReturn(Optional.of(emprunt));

        assertThatThrownBy(() -> service.retourner(5L)).isInstanceOf(ConflictException.class);
    }

    private void givenAdherent(TypeAdherent type, boolean actif) {
        when(adherentApi.getAdherent(ADHERENT_ID))
                .thenReturn(new AdherentInfo(ADHERENT_ID, "Alice Martin", type, type.quotaMax(), actif));
    }

    private void givenExemplaire(StatutExemplaire statut) {
        when(catalogueApi.getExemplaire(EXEMPLAIRE_ID))
                .thenReturn(new ExemplaireInfo(EXEMPLAIRE_ID, OUVRAGE_ID, statut));
    }
}
