package com.exam.notifications.internal.config;

import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class NotificationsOpenApiConfig {

    @Bean
    public GroupedOpenApi notificationsApiGroup() {
        return GroupedOpenApi.builder()
                .group("4-notifications")
                .pathsToMatch("/api/notifications/**")
                .build();
    }
}
