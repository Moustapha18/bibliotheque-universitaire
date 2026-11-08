package com.exam.acteurs.api;

/** Profil d'adhérent ; porte la règle de quota d'emprunts simultanés. */
public enum TypeAdherent {

    ETUDIANT(3),
    ENSEIGNANT(5);

    private final int quotaMax;

    TypeAdherent(int quotaMax) {
        this.quotaMax = quotaMax;
    }

    public int quotaMax() {
        return quotaMax;
    }
}
