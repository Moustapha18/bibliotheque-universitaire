package com.exam.shared.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

import java.time.Clock;

@Configuration
@EnableAsync
public class AppConfig {

    /** Horloge injectable : permet de figer le temps dans les tests unitaires. */
    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }
}
