package com.meuprojeto.brasileirao.dto.apifootball;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ApiTeams(
        ApiTeam home,
        ApiTeam away
) {
}
