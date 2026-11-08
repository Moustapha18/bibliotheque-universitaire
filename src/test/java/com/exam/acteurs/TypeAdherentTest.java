package com.exam.acteurs;

import com.exam.acteurs.api.TypeAdherent;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TypeAdherentTest {

    @Test
    void studentQuotaIsThree() {
        assertThat(TypeAdherent.ETUDIANT.quotaMax()).isEqualTo(3);
    }

    @Test
    void teacherQuotaIsFive() {
        assertThat(TypeAdherent.ENSEIGNANT.quotaMax()).isEqualTo(5);
    }
}
