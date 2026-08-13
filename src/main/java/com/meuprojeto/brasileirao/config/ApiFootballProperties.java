package com.meuprojeto.brasileirao.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.constraints.NotBlank;

@Validated
@ConfigurationProperties(prefix = "api-football")
public record ApiFootballProperties(
        @NotBlank String baseUrl,
        @NotBlank String key
) {
}
