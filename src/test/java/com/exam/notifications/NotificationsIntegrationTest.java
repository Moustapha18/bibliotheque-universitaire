package com.exam.notifications;

import com.exam.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Duration;

/** Vérifie la chaîne complète : transactions publie un événement, notifications le consomme. */
class NotificationsIntegrationTest extends AbstractIntegrationTest {

    @Test
    void borrowingAndReturningProduceNotificationsThroughEvents() throws Exception {
        long exemplaireId = createExemplaire(createOuvrage(createCategorie()));
        long adherent = createAdherent("ETUDIANT");

        long empruntId = idOf(emprunter(adherent, exemplaireId).andExpect(status().isCreated()));

        await().atMost(Duration.ofSeconds(10)).untilAsserted(() ->
                mockMvc.perform(get("/api/notifications").param("adherentId", String.valueOf(adherent)))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.content", hasSize(1)))
                        .andExpect(jsonPath("$.content[0].type").value("RAPPEL_RETOUR")));

        mockMvc.perform(post("/api/emprunts/{id}/retour", empruntId)).andExpect(status().isOk());

        await().atMost(Duration.ofSeconds(10)).untilAsserted(() ->
                mockMvc.perform(get("/api/notifications").param("adherentId", String.valueOf(adherent)))
                        .andExpect(jsonPath("$.content", hasSize(2)))
                        .andExpect(jsonPath("$.content[0].type").value("CONFIRMATION_RETOUR")));
    }
}
