package com.exam.catalogue.internal.config;

import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CatalogueOpenApiConfig {

    @Bean
    public GroupedOpenApi catalogueApiGroup() {
        return GroupedOpenApi.builder()
                .group("1-catalogue")
                .pathsToMatch("/api/categories/**", "/api/ouvrages/**", "/api/exemplaires/**")
                .build();
    }
}
