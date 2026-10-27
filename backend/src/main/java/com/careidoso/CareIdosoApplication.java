package com.careidoso;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.time.ZoneId;
import java.util.TimeZone;

/**
 * Ponto de entrada do backend Care Idoso (Spring Boot).
 */
@SpringBootApplication
public class CareIdosoApplication {

    /**
     * Fixa o fuso padrão da JVM em America/Sao_Paulo, coerente com
     * {@code hibernate.jdbc.time_zone}, para que datas/horas sem fuso (LocalDateTime)
     * sejam interpretadas no horário local dos plantões. Campos {@code Instant}
     * (tokens e códigos) são independentes de fuso.
     */
    @PostConstruct
    public void init() {
        TimeZone.setDefault(TimeZone.getTimeZone(ZoneId.of("America/Sao_Paulo")));
    }

    public static void main(String[] args) {
        SpringApplication.run(CareIdosoApplication.class, args);
    }
}
