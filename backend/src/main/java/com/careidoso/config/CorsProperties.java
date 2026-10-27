package com.careidoso.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Origens permitidas pelo CORS, lidas de {@code careidoso.cors.allowed-origins}.
 */
@Component
@Getter
public class CorsProperties {

    /**
     * Lista de origens separadas por vírgula (ex.: a URL do frontend). Em produção deve
     * conter apenas origens HTTPS confiáveis; nunca usar curinga, pois o CORS é
     * configurado com credenciais habilitadas.
     */
    @Value("${careidoso.cors.allowed-origins}")
    private String allowedOrigins;
}
