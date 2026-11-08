package com.exam.catalogue.internal.dto;

import com.exam.catalogue.api.StatutExemplaire;

public record ExemplaireResponse(Long id, Long ouvrageId, String codeBarre, StatutExemplaire statut) {
}
