package com.exam.transactions;

import com.exam.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class TransactionsIntegrationTest extends AbstractIntegrationTest {

    @Test
    void fullCycle_borrow_reserve_return_thenReservedCopyGoesToTheWaitingMember() throws Exception {
        long ouvrageId = createOuvrage(createCategorie());
        long exemplaireId = createExemplaire(ouvrageId);
        long alice = createAdherent("ETUDIANT");
        long bob = createAdherent("ETUDIANT");

        long empruntId = idOf(emprunter(alice, exemplaireId).andExpect(status().isCreated()));
        emprunter(bob, exemplaireId).andExpect(status().isConflict());

        long reservationId = idOf(postJson("/api/reservations", Map.of("ouvrageId", ouvrageId, "adherentId", bob))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.statut").value("EN_ATTENTE")));

        mockMvc.perform(post("/api/emprunts/{id}/retour", empruntId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.joursRetard").value(0))
                .andExpect(jsonPath("$.penalite").value(0));

        mockMvc.perform(get("/api/reservations/{id}", reservationId))
                .andExpect(jsonPath("$.statut").value("PRETE"))
                .andExpect(jsonPath("$.exemplaireId").value(exemplaireId));
        mockMvc.perform(get("/api/exemplaires/{id}", exemplaireId))
                .andExpect(jsonPath("$.statut").value("RESERVE"));

        emprunter(alice, exemplaireId).andExpect(status().isConflict());
        emprunter(bob, exemplaireId).andExpect(status().isCreated());

        mockMvc.perform(get("/api/reservations/{id}", reservationId))
                .andExpect(jsonPath("$.statut").value("HONOREE"));
        mockMvc.perform(get("/api/exemplaires/{id}", exemplaireId))
                .andExpect(jsonPath("$.statut").value("EMPRUNTE"));
    }

    @Test
    void returnWithoutWaitingReservationMakesTheCopyAvailableAgain() throws Exception {
        long exemplaireId = createExemplaire(createOuvrage(createCategorie()));
        long adherent = createAdherent("ENSEIGNANT");

        long empruntId = idOf(emprunter(adherent, exemplaireId).andExpect(status().isCreated()));
        mockMvc.perform(post("/api/emprunts/{id}/retour", empruntId)).andExpect(status().isOk());

        mockMvc.perform(get("/api/exemplaires/{id}", exemplaireId)).andExpect(jsonPath("$.statut").value("DISPONIBLE"));
        mockMvc.perform(post("/api/emprunts/{id}/retour", empruntId)).andExpect(status().isConflict());
    }

    @Test
    void studentCannotExceedQuotaOfThree() throws Exception {
        long ouvrageId = createOuvrage(createCategorie());
        long etudiant = createAdherent("ETUDIANT");

        for (int i = 0; i < 3; i++) {
            emprunter(etudiant, createExemplaire(ouvrageId)).andExpect(status().isCreated());
        }
        emprunter(etudiant, createExemplaire(ouvrageId))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(409));
    }

    @Test
    void cannotReserveWhenACopyIsAvailable() throws Exception {
        long ouvrageId = createOuvrage(createCategorie());
        createExemplaire(ouvrageId);
        long adherent = createAdherent("ETUDIANT");

        postJson("/api/reservations", Map.of("ouvrageId", ouvrageId, "adherentId", adherent))
                .andExpect(status().isConflict());
    }

    @Test
    void borrowingAnUnknownCopyReturnsNotFound() throws Exception {
        long adherent = createAdherent("ETUDIANT");
        emprunter(adherent, 999_999).andExpect(status().isNotFound());
    }
}
