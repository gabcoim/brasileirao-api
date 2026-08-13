package com.meuprojeto.brasileirao.dto.apifootball;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.OffsetDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ApiFixture(
        Long id,
        OffsetDateTime date,
        ApiFixtureStatus status
) {
}
