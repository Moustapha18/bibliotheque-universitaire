package com.exam.transactions.internal.web;

import com.exam.shared.web.ApiErrors;
import com.exam.shared.web.PageResponse;
import com.exam.transactions.internal.dto.EmpruntRequest;
import com.exam.transactions.internal.dto.EmpruntResponse;
import com.exam.transactions.internal.service.EmpruntService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/emprunts")
@RequiredArgsConstructor
@Tag(name = "Emprunts")
public class EmpruntController {

    private final EmpruntService service;

    @Operation(summary = "Emprunter un exemplaire",
            description = "Refusé (409) si l'exemplaire est déjà emprunté ou réservé pour un autre, "
                    + "ou si l'adhérent a atteint son quota (3 étudiant, 5 enseignant).")
    @ApiResponse(responseCode = "201", description = "Emprunt enregistré")
    @ApiErrors
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EmpruntResponse emprunter(@Valid @RequestBody EmpruntRequest request) {
        return service.emprunter(request);
    }

    @Operation(summary = "Retourner un exemplaire",
            description = "Calcule le retard et la pénalité ; l'exemplaire passe au premier de la file de réservation s'il y en a un.")
    @ApiResponse(responseCode = "200", description = "Retour enregistré")
    @ApiErrors
    @PostMapping("/{id}/retour")
    public EmpruntResponse retourner(@PathVariable Long id) {
        return service.retourner(id);
    }

    @Operation(summary = "Consulter un emprunt")
    @ApiResponse(responseCode = "200", description = "Emprunt trouvé")
    @ApiErrors
    @GetMapping("/{id}")
    public EmpruntResponse findById(@PathVariable Long id) {
        return service.findById(id);
    }

    @Operation(summary = "Lister les emprunts, éventuellement ceux d'un adhérent")
    @ApiResponse(responseCode = "200", description = "Page d'emprunts")
    @ApiErrors
    @GetMapping
    public PageResponse<EmpruntResponse> findAll(
            @Parameter(description = "Filtrer sur un adhérent") @RequestParam(required = false) Long adherentId,
            @ParameterObject @PageableDefault(size = 20, sort = "dateEmprunt", direction = Sort.Direction.DESC) Pageable pageable) {
        return PageResponse.from(service.findAll(adherentId, pageable));
    }
}
