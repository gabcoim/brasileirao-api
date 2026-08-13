package com.meuprojeto.brasileirao.dto.apifootball;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ApiTeam(
        Long id,
        String name,
        String logo
) {
}
