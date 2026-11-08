package com.exam.acteurs.internal.config;

import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ActeursOpenApiConfig {

    @Bean
    public GroupedOpenApi acteursApiGroup() {
        return GroupedOpenApi.builder()
                .group("2-acteurs")
                .pathsToMatch("/api/adherents/**")
                .build();
    }
}
