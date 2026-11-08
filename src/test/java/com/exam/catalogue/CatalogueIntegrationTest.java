package com.exam.catalogue;

import com.exam.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CatalogueIntegrationTest extends AbstractIntegrationTest {

    @Test
    void searchFiltersByTitleAndAvailability() throws Exception {
        String marker = "Zebra" + unique();
        long categorieId = createCategorie();
        long avecExemplaire = createOuvrage(categorieId, marker + " avec exemplaire");
        createOuvrage(categorieId, marker + " sans exemplaire");
        createExemplaire(avecExemplaire);

        mockMvc.perform(get("/api/ouvrages/search").param("titre", marker.toLowerCase()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2));

        mockMvc.perform(get("/api/ouvrages/search").param("titre", marker).param("disponible", "true"))
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].id").value(avecExemplaire));

        mockMvc.perform(get("/api/ouvrages/search").param("titre", marker).param("disponible", "false"))
                .andExpect(jsonPath("$.content", hasSize(1)));
    }

    @Test
    void unknownOuvrageReturnsUniformNotFoundBody() throws Exception {
        mockMvc.perform(get("/api/ouvrages/{id}", 999_999))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void invalidOuvrageReturnsValidationDetails() throws Exception {
        postJson("/api/ouvrages", Map.of("titre", "", "isbn", "abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.details").isNotEmpty());
    }

    @Test
    void deletingCategoryUsedByAnOuvrageIsRefused() throws Exception {
        long categorieId = createCategorie();
        createOuvrage(categorieId);

        mockMvc.perform(delete("/api/categories/{id}", categorieId))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(409));
    }

    @Test
    void duplicateIsbnIsRefused() throws Exception {
        long categorieId = createCategorie();
        Map<String, Object> body = Map.of("titre", "Doublon", "auteur", "A", "isbn", "9781234567897",
                "categorieId", categorieId);
        postJson("/api/ouvrages", body).andExpect(status().isCreated());
        postJson("/api/ouvrages", body).andExpect(status().isConflict());
    }

    @Test
    void paginationAndSortingWork() throws Exception {
        mockMvc.perform(get("/api/categories").param("page", "0").param("size", "2").param("sort", "nom,desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size").value(2))
                .andExpect(jsonPath("$.content", hasSize(2)));

        mockMvc.perform(get("/api/categories").param("sort", "inexistant"))
                .andExpect(status().isBadRequest());
    }
}
