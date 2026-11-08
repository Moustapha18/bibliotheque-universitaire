package com.exam.catalogue.internal.domain;

import com.exam.catalogue.api.StatutExemplaire;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.Locale;

/** Construction des filtres de recherche d'ouvrages. */
public final class OuvrageSpecifications {

    private static final char ESCAPE = '\\';

    private OuvrageSpecifications() {
    }

    public static Specification<Ouvrage> from(OuvrageSearchCritere criteria) {
        Specification<Ouvrage> spec = Specification.where(null);
        if (StringUtils.hasText(criteria.titre())) {
            spec = spec.and(containsIgnoreCase("titre", criteria.titre()));
        }
        if (StringUtils.hasText(criteria.auteur())) {
            spec = spec.and(containsIgnoreCase("auteur", criteria.auteur()));
        }
        if (criteria.categorieId() != null) {
            spec = spec.and(inCategorie(criteria.categorieId()));
        }
        if (criteria.disponible() != null) {
            spec = spec.and(availability(criteria.disponible()));
        }
        return spec;
    }

    private static Specification<Ouvrage> containsIgnoreCase(String attribute, String value) {
        return (root, query, cb) -> cb.like(cb.lower(root.get(attribute)), likePattern(value), ESCAPE);
    }

    private static Specification<Ouvrage> inCategorie(Long categorieId) {
        return (root, query, cb) -> cb.equal(root.get("categorie").get("id"), categorieId);
    }

    private static Specification<Ouvrage> availability(boolean disponible) {
        return (root, query, cb) -> {
            Subquery<Long> copies = query.subquery(Long.class);
            Root<Exemplaire> exemplaire = copies.from(Exemplaire.class);
            copies.select(exemplaire.get("id"))
                    .where(cb.equal(exemplaire.get("ouvrage"), root),
                            cb.equal(exemplaire.get("statut"), StatutExemplaire.DISPONIBLE));
            return disponible ? cb.exists(copies) : cb.not(cb.exists(copies));
        };
    }

    private static String likePattern(String value) {
        String escaped = value.toLowerCase(Locale.ROOT)
                .replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
        return "%" + escaped + "%";
    }
}
