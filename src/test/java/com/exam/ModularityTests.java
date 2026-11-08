package com.exam;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModule;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.docs.Documenter;

import static org.assertj.core.api.Assertions.assertThat;

/** Test d'architecture : vérifie isolation des modules, API publiques et absence de cycles. */
class ModularityTests {

    private final ApplicationModules modules = ApplicationModules.of(LibraryApplication.class);

    @Test
    void verifiesModularStructure() {
        modules.verify();
    }

    @Test
    void detectsTheFourBusinessModules() {
        assertThat(modules.stream().map(ApplicationModule::getName))
                .contains("catalogue", "acteurs", "transactions", "notifications");
    }

    @Test
    void generatesModuleDocumentation() {
        new Documenter(modules)
                .writeModulesAsPlantUml()
                .writeIndividualModulesAsPlantUml();
    }
}
