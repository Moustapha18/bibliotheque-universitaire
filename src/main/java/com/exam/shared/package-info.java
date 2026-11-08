/**
 * Socle technique commun : erreurs, configuration, types partagés. Aucune logique métier.
 * Module "ouvert" : les autres modules peuvent utiliser tous ses types.
 */
@org.springframework.modulith.ApplicationModule(
        displayName = "Shared",
        type = org.springframework.modulith.ApplicationModule.Type.OPEN)
package com.exam.shared;
