package com.meuprojeto.brasileirao.dto.apifootball;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ApiLeague(
        Long id,
        String name,
        Integer season,
        String round
) {
}
