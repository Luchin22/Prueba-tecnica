package com.banco.cliente.config;

import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.util.TimeZone;

@Configuration
@EnableJpaAuditing
public class JpaConfig {

    @PostConstruct
    public void init() {
        // Configurar la JVM para usar zona horaria de Ecuador
        TimeZone.setDefault(TimeZone.getTimeZone("America/Guayaquil"));
    }
}
