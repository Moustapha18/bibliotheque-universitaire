package com.exam.acteurs;

import com.exam.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ActeursIntegrationTest extends AbstractIntegrationTest {

    @Test
    void studentGetsQuotaOfThreeAndTeacherQuotaOfFive() throws Exception {
        long etudiant = createAdherent("ETUDIANT");
        long enseignant = createAdherent("ENSEIGNANT");

        mockMvc.perform(get("/api/adherents/{id}", etudiant)).andExpect(jsonPath("$.quotaMax").value(3));
        mockMvc.perform(get("/api/adherents/{id}", enseignant)).andExpect(jsonPath("$.quotaMax").value(5));
    }

    @Test
    void duplicateEmailIsRefused() throws Exception {
        Map<String, Object> body = Map.of("nom", "Sow", "prenom", "Awa", "email", unique() + "@test.sn", "type", "ETUDIANT");
        postJson("/api/adherents", body).andExpect(status().isCreated());
        postJson("/api/adherents", body).andExpect(status().isConflict());
    }

    @Test
    void invalidEmailReturnsValidationDetails() throws Exception {
        postJson("/api/adherents", Map.of("nom", "Sow", "prenom", "Awa", "email", "pas-un-email", "type", "ETUDIANT"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details[0].field").value("email"));
    }

    @Test
    void deleteDeactivatesTheAdherent() throws Exception {
        long id = createAdherent("ETUDIANT");

        mockMvc.perform(delete("/api/adherents/{id}", id)).andExpect(status().isNoContent());
        mockMvc.perform(get("/api/adherents/{id}", id)).andExpect(jsonPath("$.actif").value(false));
    }
}
