package com.meuprojeto.brasileirao.dto.apifootball;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import static org.assertj.core.api.Assertions.assertThat;

class ApiFixturesResponseTest {

    private final ObjectMapper objectMapper = JsonMapper.builder().build();

    @Test
    void deveConverterRespostaDaApi() throws Exception {
        String json = """
                {
                  "results": 1,
                  "errors": [],
                  "response": [{
                    "fixture": {
                      "id": 12345,
                      "date": "2026-04-01T19:00:00-03:00",
                      "status": {"long": "Not Started", "short": "NS"}
                    },
                    "league": {
                      "id": 71,
                      "name": "Serie A",
                      "season": 2026,
                      "round": "Regular Season - 1"
                    },
                    "teams": {
                      "home": {"id": 10, "name": "Mandante", "logo": "mandante.png"},
                      "away": {"id": 20, "name": "Visitante", "logo": "visitante.png"}
                    },
                    "goals": {"home": null, "away": null}
                  }]
                }
                """;

        ApiFixturesResponse resposta = objectMapper.readValue(json, ApiFixturesResponse.class);

        assertThat(resposta.results()).isEqualTo(1);
        assertThat(resposta.possuiErros()).isFalse();
        assertThat(resposta.response()).hasSize(1);
        assertThat(resposta.response().getFirst().fixture().status().shortStatus()).isEqualTo("NS");
        assertThat(resposta.response().getFirst().fixture().date().getYear()).isEqualTo(2026);
    }
}
