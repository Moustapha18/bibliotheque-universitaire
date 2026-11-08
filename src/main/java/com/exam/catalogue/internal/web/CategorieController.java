package com.exam.catalogue.internal.web;

import com.exam.catalogue.internal.dto.CategorieRequest;
import com.exam.catalogue.internal.dto.CategorieResponse;
import com.exam.catalogue.internal.service.CategorieService;
import com.exam.shared.web.ApiErrors;
import com.exam.shared.web.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@Tag(name = "Catégories")
public class CategorieController {

    private final CategorieService service;

    @Operation(summary = "Lister les catégories (pagination et tri)")
    @ApiResponse(responseCode = "200", description = "Page de catégories")
    @ApiErrors
    @GetMapping
    public PageResponse<CategorieResponse> findAll(@ParameterObject @PageableDefault(size = 20, sort = "nom") Pageable pageable) {
        return PageResponse.from(service.findAll(pageable));
    }

    @Operation(summary = "Consulter une catégorie")
    @ApiResponse(responseCode = "200", description = "Catégorie trouvée")
    @ApiErrors
    @GetMapping("/{id}")
    public CategorieResponse findById(@PathVariable Long id) {
        return service.findById(id);
    }

    @Operation(summary = "Créer une catégorie")
    @ApiResponse(responseCode = "201", description = "Catégorie créée")
    @ApiErrors
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategorieResponse create(@Valid @RequestBody CategorieRequest request) {
        return service.create(request);
    }

    @Operation(summary = "Modifier une catégorie")
    @ApiResponse(responseCode = "200", description = "Catégorie modifiée")
    @ApiErrors
    @PutMapping("/{id}")
    public CategorieResponse update(@PathVariable Long id, @Valid @RequestBody CategorieRequest request) {
        return service.update(id, request);
    }

    @Operation(summary = "Supprimer une catégorie (refusé si des ouvrages l'utilisent)")
    @ApiResponse(responseCode = "204", description = "Catégorie supprimée")
    @ApiErrors
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
