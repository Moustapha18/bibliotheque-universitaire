package com.exam.transactions.internal.config;

import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TransactionsOpenApiConfig {

    @Bean
    public GroupedOpenApi transactionsApiGroup() {
        return GroupedOpenApi.builder()
                .group("3-transactions")
                .pathsToMatch("/api/emprunts/**", "/api/reservations/**")
                .build();
    }
}
