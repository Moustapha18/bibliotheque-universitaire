package com.exam.catalogue.internal.web;

import com.exam.catalogue.internal.domain.OuvrageSearchCritere;
import com.exam.catalogue.internal.dto.OuvrageRequest;
import com.exam.catalogue.internal.dto.OuvrageResponse;
import com.exam.catalogue.internal.service.OuvrageService;
import com.exam.shared.web.ApiErrors;
import com.exam.shared.web.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ouvrages")
@RequiredArgsConstructor
@Tag(name = "Ouvrages")
public class OuvrageController {

    private final OuvrageService service;

    @Operation(summary = "Lister les ouvrages (pagination et tri)")
    @ApiResponse(responseCode = "200", description = "Page d'ouvrages")
    @ApiErrors
    @GetMapping
    public PageResponse<OuvrageResponse> findAll(@ParameterObject @PageableDefault(size = 20, sort = "titre") Pageable pageable) {
        return PageResponse.from(service.findAll(pageable));
    }

    @Operation(summary = "Recherche multicritère (titre, auteur, catégorie, disponibilité)")
    @ApiResponse(responseCode = "200", description = "Page d'ouvrages correspondant aux critères")
    @ApiErrors
    @GetMapping("/search")
    public PageResponse<OuvrageResponse> search(
            @Parameter(description = "Fragment du titre", example = "clean") @RequestParam(required = false) String titre,
            @Parameter(description = "Fragment du nom de l'auteur", example = "martin") @RequestParam(required = false) String auteur,
            @Parameter(description = "Identifiant de la catégorie") @RequestParam(required = false) Long categorieId,
            @Parameter(description = "true : au moins un exemplaire disponible ; false : aucun")
            @RequestParam(required = false) Boolean disponible,
            @ParameterObject @PageableDefault(size = 20, sort = "titre") Pageable pageable) {
        OuvrageSearchCritere criteria = new OuvrageSearchCritere(titre, auteur, categorieId, disponible);
        return PageResponse.from(service.search(criteria, pageable));
    }

    @Operation(summary = "Consulter un ouvrage")
    @ApiResponse(responseCode = "200", description = "Ouvrage trouvé")
    @ApiErrors
    @GetMapping("/{id}")
    public OuvrageResponse findById(@PathVariable Long id) {
        return service.findById(id);
    }

    @Operation(summary = "Créer un ouvrage")
    @ApiResponse(responseCode = "201", description = "Ouvrage créé")
    @ApiErrors
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OuvrageResponse create(@Valid @RequestBody OuvrageRequest request) {
        return service.create(request);
    }

    @Operation(summary = "Modifier un ouvrage")
    @ApiResponse(responseCode = "200", description = "Ouvrage modifié")
    @ApiErrors
    @PutMapping("/{id}")
    public OuvrageResponse update(@PathVariable Long id, @Valid @RequestBody OuvrageRequest request) {
        return service.update(id, request);
    }

    @Operation(summary = "Supprimer un ouvrage (refusé s'il possède des exemplaires)")
    @ApiResponse(responseCode = "204", description = "Ouvrage supprimé")
    @ApiErrors
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
