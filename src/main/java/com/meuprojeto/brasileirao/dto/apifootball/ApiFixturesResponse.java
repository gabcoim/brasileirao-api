package com.meuprojeto.brasileirao.dto.apifootball;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ApiFixturesResponse(
        Integer results,
        Object errors,
        List<ApiFixtureItem> response
) {
    public ApiFixturesResponse {
        response = response == null ? List.of() : response;
    }

    public boolean possuiErros() {
        if (errors == null) {
            return false;
        }
        if (errors instanceof Collection<?> colecao) {
            return !colecao.isEmpty();
        }
        if (errors instanceof Map<?, ?> mapa) {
            return !mapa.isEmpty();
        }
        return true;
    }
}
