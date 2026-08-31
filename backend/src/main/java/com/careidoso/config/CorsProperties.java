package com.careidoso.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Getter
public class CorsProperties {

    @Value("${careidoso.cors.allowed-origins}")
    private String allowedOrigins;
}
