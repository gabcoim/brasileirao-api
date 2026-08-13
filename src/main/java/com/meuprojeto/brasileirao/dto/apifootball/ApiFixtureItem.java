package com.meuprojeto.brasileirao.dto.apifootball;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ApiFixtureItem(
        ApiFixture fixture,
        ApiLeague league,
        ApiTeams teams,
        ApiGoals goals
) {
}
