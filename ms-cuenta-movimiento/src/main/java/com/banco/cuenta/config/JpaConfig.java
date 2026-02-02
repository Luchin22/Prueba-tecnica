package com.banco.cuenta.config;

import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.util.TimeZone;

@Configuration
@EnableJpaAuditing
@EnableJpaRepositories(basePackages = "com.banco.cuenta.repository")
@EnableTransactionManagement
public class JpaConfig {

    @PostConstruct
    public void init() {
        // configurar la jvm para usar zona horaria de ecuador
        TimeZone.setDefault(TimeZone.getTimeZone("America/Guayaquil"));
    }
}
