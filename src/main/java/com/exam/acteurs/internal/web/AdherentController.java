package com.exam.acteurs.internal.web;

import com.exam.acteurs.internal.dto.AdherentRequest;
import com.exam.acteurs.internal.dto.AdherentResponse;
import com.exam.acteurs.internal.service.AdherentService;
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
@RequestMapping("/api/adherents")
@RequiredArgsConstructor
@Tag(name = "Adhérents")
public class AdherentController {

    private final AdherentService service;

    @Operation(summary = "Lister les adhérents (pagination et tri)")
    @ApiResponse(responseCode = "200", description = "Page d'adhérents")
    @ApiErrors
    @GetMapping
    public PageResponse<AdherentResponse> findAll(@ParameterObject @PageableDefault(size = 20, sort = "nom") Pageable pageable) {
        return PageResponse.from(service.findAll(pageable));
    }

    @Operation(summary = "Consulter un adhérent")
    @ApiResponse(responseCode = "200", description = "Adhérent trouvé")
    @ApiErrors
    @GetMapping("/{id}")
    public AdherentResponse findById(@PathVariable Long id) {
        return service.findById(id);
    }

    @Operation(summary = "Créer un adhérent (quota : 3 pour un étudiant, 5 pour un enseignant)")
    @ApiResponse(responseCode = "201", description = "Adhérent créé")
    @ApiErrors
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AdherentResponse create(@Valid @RequestBody AdherentRequest request) {
        return service.create(request);
    }

    @Operation(summary = "Modifier un adhérent")
    @ApiResponse(responseCode = "200", description = "Adhérent modifié")
    @ApiErrors
    @PutMapping("/{id}")
    public AdherentResponse update(@PathVariable Long id, @Valid @RequestBody AdherentRequest request) {
        return service.update(id, request);
    }

    @Operation(summary = "Désactiver un adhérent (suppression logique)")
    @ApiResponse(responseCode = "204", description = "Adhérent désactivé")
    @ApiErrors
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deactivate(@PathVariable Long id) {
        service.deactivate(id);
    }
}
