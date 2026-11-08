package com.exam.transactions.internal.web;

import com.exam.shared.web.ApiErrors;
import com.exam.shared.web.PageResponse;
import com.exam.transactions.internal.dto.ReservationRequest;
import com.exam.transactions.internal.dto.ReservationResponse;
import com.exam.transactions.internal.service.ReservationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
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
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
@Tag(name = "Réservations")
public class ReservationController {

    private final ReservationService service;

    @Operation(summary = "Réserver un ouvrage indisponible",
            description = "Refusé (409) si un exemplaire est disponible ou si l'adhérent a déjà une réservation active.")
    @ApiResponse(responseCode = "201", description = "Réservation placée dans la file d'attente")
    @ApiErrors
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReservationResponse reserver(@Valid @RequestBody ReservationRequest request) {
        return service.reserver(request);
    }

    @Operation(summary = "Annuler une réservation")
    @ApiResponse(responseCode = "200", description = "Réservation annulée")
    @ApiErrors
    @PostMapping("/{id}/annulation")
    public ReservationResponse annuler(@PathVariable Long id) {
        return service.annuler(id);
    }

    @Operation(summary = "Consulter une réservation")
    @ApiResponse(responseCode = "200", description = "Réservation trouvée")
    @ApiErrors
    @GetMapping("/{id}")
    public ReservationResponse findById(@PathVariable Long id) {
        return service.findById(id);
    }

    @Operation(summary = "Lister les réservations, éventuellement celles d'un adhérent")
    @ApiResponse(responseCode = "200", description = "Page de réservations")
    @ApiErrors
    @GetMapping
    public PageResponse<ReservationResponse> findAll(
            @Parameter(description = "Filtrer sur un adhérent") @RequestParam(required = false) Long adherentId,
            @ParameterObject @PageableDefault(size = 20, sort = "dateReservation", direction = Sort.Direction.DESC) Pageable pageable) {
        return PageResponse.from(service.findAll(adherentId, pageable));
    }
}
