package com.exam.catalogue.internal.web;

import com.exam.catalogue.internal.dto.ExemplaireRequest;
import com.exam.catalogue.internal.dto.ExemplaireResponse;
import com.exam.catalogue.internal.service.ExemplaireService;
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
@RequestMapping("/api/exemplaires")
@RequiredArgsConstructor
@Tag(name = "Exemplaires")
public class ExemplaireController {

    private final ExemplaireService service;

    @Operation(summary = "Lister les exemplaires, éventuellement ceux d'un ouvrage")
    @ApiResponse(responseCode = "200", description = "Page d'exemplaires")
    @ApiErrors
    @GetMapping
    public PageResponse<ExemplaireResponse> findAll(
            @Parameter(description = "Filtrer sur un ouvrage") @RequestParam(required = false) Long ouvrageId,
            @ParameterObject @PageableDefault(size = 20, sort = "codeBarre") Pageable pageable) {
        return PageResponse.from(service.findAll(ouvrageId, pageable));
    }

    @Operation(summary = "Consulter un exemplaire")
    @ApiResponse(responseCode = "200", description = "Exemplaire trouvé")
    @ApiErrors
    @GetMapping("/{id}")
    public ExemplaireResponse findById(@PathVariable Long id) {
        return service.findById(id);
    }

    @Operation(summary = "Créer un exemplaire (statut initial : DISPONIBLE)")
    @ApiResponse(responseCode = "201", description = "Exemplaire créé")
    @ApiErrors
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ExemplaireResponse create(@Valid @RequestBody ExemplaireRequest request) {
        return service.create(request);
    }

    @Operation(summary = "Modifier un exemplaire")
    @ApiResponse(responseCode = "200", description = "Exemplaire modifié")
    @ApiErrors
    @PutMapping("/{id}")
    public ExemplaireResponse update(@PathVariable Long id, @Valid @RequestBody ExemplaireRequest request) {
        return service.update(id, request);
    }

    @Operation(summary = "Supprimer un exemplaire (uniquement s'il est DISPONIBLE)")
    @ApiResponse(responseCode = "204", description = "Exemplaire supprimé")
    @ApiErrors
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
