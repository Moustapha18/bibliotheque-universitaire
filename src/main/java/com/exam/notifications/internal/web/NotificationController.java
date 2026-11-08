package com.exam.notifications.internal.web;

import com.exam.notifications.api.NotificationApi;
import com.exam.notifications.api.NotificationDto;
import com.exam.shared.web.ApiErrors;
import com.exam.shared.web.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications")
public class NotificationController {

    private final NotificationApi notificationApi;

    @Operation(summary = "Consulter les notifications (rappels de retour, alertes de retard...)")
    @ApiResponse(responseCode = "200", description = "Page de notifications, les plus récentes d'abord")
    @ApiErrors
    @GetMapping
    public PageResponse<NotificationDto> findAll(
            @Parameter(description = "Filtrer sur un adhérent") @RequestParam(required = false) Long adherentId,
            @ParameterObject @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return PageResponse.from(notificationApi.rechercher(adherentId, pageable));
    }
}
