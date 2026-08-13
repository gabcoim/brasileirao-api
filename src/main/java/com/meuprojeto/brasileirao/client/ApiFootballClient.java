package com.meuprojeto.brasileirao.client;

import com.meuprojeto.brasileirao.dto.apifootball.ApiFixturesResponse;
import com.meuprojeto.brasileirao.config.ApiFootballProperties;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@Profile("!demo")
public class ApiFootballClient {

    private final RestClient restClient;

    public ApiFootballClient(
            RestClient.Builder builder,
            ApiFootballProperties properties
    ) {
        this.restClient = builder
                .baseUrl(properties.baseUrl())
                .defaultHeader("x-apisports-key", properties.key())
                .build();
    }

    public ApiFixturesResponse buscarPartidas(
            Long ligaId,
            Integer temporada
    ) {
        return restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/fixtures")
                        .queryParam("league", ligaId)
                        .queryParam("season", temporada)
                        .build()
                )
                .retrieve()
                .body(ApiFixturesResponse.class);
    }
}
