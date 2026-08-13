package com.meuprojeto.brasileirao.service;

import com.meuprojeto.brasileirao.dto.ResultadoImportacaoPartidas;
import com.meuprojeto.brasileirao.dto.apifootball.ApiFixture;
import com.meuprojeto.brasileirao.dto.apifootball.ApiFixtureItem;
import com.meuprojeto.brasileirao.dto.apifootball.ApiFixtureStatus;
import com.meuprojeto.brasileirao.dto.apifootball.ApiGoals;
import com.meuprojeto.brasileirao.dto.apifootball.ApiLeague;
import com.meuprojeto.brasileirao.dto.apifootball.ApiTeam;
import com.meuprojeto.brasileirao.dto.apifootball.ApiTeams;
import com.meuprojeto.brasileirao.model.StatusPartida;
import com.meuprojeto.brasileirao.repository.PartidaRepository;
import com.meuprojeto.brasileirao.repository.TimeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class PartidaPersistenciaServiceTest {

    @Autowired
    private PartidaPersistenciaService service;

    @Autowired
    private PartidaRepository partidaRepository;

    @Autowired
    private TimeRepository timeRepository;

    @BeforeEach
    void limparBanco() {
        partidaRepository.deleteAll();
        timeRepository.deleteAll();
    }

    @Test
    void deveCriarEDepoisAtualizarSemDuplicar() {
        ApiFixtureItem agendada = criarItem("NS", null, null);
        ResultadoImportacaoPartidas primeiraImportacao = service.salvar(List.of(agendada));

        ApiFixtureItem finalizada = criarItem("FT", 2, 1);
        ResultadoImportacaoPartidas segundaImportacao = service.salvar(List.of(finalizada));

        assertThat(primeiraImportacao.criadas()).isEqualTo(1);
        assertThat(segundaImportacao.atualizadas()).isEqualTo(1);
        assertThat(partidaRepository.count()).isEqualTo(1);
        assertThat(timeRepository.count()).isEqualTo(2);
        assertThat(partidaRepository.findByIdExterno(12345L).orElseThrow().getStatus())
                .isEqualTo(StatusPartida.FINALIZADA);
        assertThat(partidaRepository.findByIdExterno(12345L).orElseThrow().getPlacarMandante())
                .isEqualTo(2);
    }

    private ApiFixtureItem criarItem(String status, Integer golsMandante, Integer golsVisitante) {
        return new ApiFixtureItem(
                new ApiFixture(
                        12345L,
                        OffsetDateTime.parse("2026-04-01T19:00:00-03:00"),
                        new ApiFixtureStatus("Status", status)
                ),
                new ApiLeague(71L, "Serie A", 2026, "Regular Season - 1"),
                new ApiTeams(
                        new ApiTeam(10L, "Mandante", "mandante.png"),
                        new ApiTeam(20L, "Visitante", "visitante.png")
                ),
                new ApiGoals(golsMandante, golsVisitante)
        );
    }
}
