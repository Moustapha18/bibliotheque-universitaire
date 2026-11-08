package com.exam;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.testcontainers.containers.PostgreSQLContainer;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Base des tests d'intégration : un seul PostgreSQL (Testcontainers) partagé par toutes les classes,
 * avec les vraies migrations Flyway et ddl-auto=validate.
 */
@SpringBootTest
@AutoConfigureMockMvc
public abstract class AbstractIntegrationTest {

    private static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    static {
        POSTGRES.start();
    }

    @DynamicPropertySource
    static void datasourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    protected ResultActions postJson(String url, Object body) throws Exception {
        return mockMvc.perform(post(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)));
    }

    protected long idOf(ResultActions result) throws Exception {
        String json = result.andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(json).get("id").asLong();
    }

    protected long createCategorie() throws Exception {
        return idOf(postJson("/api/categories", Map.of("nom", "Cat-" + unique())).andExpect(status().isCreated()));
    }

    protected long createOuvrage(long categorieId) throws Exception {
        return createOuvrage(categorieId, "Titre-" + unique());
    }

    protected long createOuvrage(long categorieId, String titre) throws Exception {
        Map<String, Object> body = Map.of(
                "titre", titre,
                "auteur", "Auteur Test",
                "isbn", String.valueOf(ThreadLocalRandom.current().nextLong(1_000_000_000_000L, 9_999_999_999_999L)),
                "anneePublication", 2020,
                "categorieId", categorieId);
        return idOf(postJson("/api/ouvrages", body).andExpect(status().isCreated()));
    }

    protected long createExemplaire(long ouvrageId) throws Exception {
        Map<String, Object> body = Map.of("ouvrageId", ouvrageId, "codeBarre", "EX-" + unique());
        return idOf(postJson("/api/exemplaires", body).andExpect(status().isCreated()));
    }

    protected long createAdherent(String type) throws Exception {
        Map<String, Object> body = Map.of(
                "nom", "Nom", "prenom", "Prenom", "email", unique() + "@test.sn", "type", type);
        return idOf(postJson("/api/adherents", body).andExpect(status().isCreated()));
    }

    protected ResultActions emprunter(long adherentId, long exemplaireId) throws Exception {
        return postJson("/api/emprunts", Map.of("adherentId", adherentId, "exemplaireId", exemplaireId));
    }

    protected static String unique() {
        return UUID.randomUUID().toString().substring(0, 8);
    }
}
