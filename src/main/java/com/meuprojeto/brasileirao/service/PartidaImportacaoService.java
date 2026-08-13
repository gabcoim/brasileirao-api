package com.meuprojeto.brasileirao.service;

import com.meuprojeto.brasileirao.client.ApiFootballClient;
import com.meuprojeto.brasileirao.dto.ResultadoImportacaoPartidas;
import com.meuprojeto.brasileirao.dto.apifootball.ApiFixturesResponse;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("!demo")
public class PartidaImportacaoService {

    private final ApiFootballClient apiFootballClient;
    private final PartidaPersistenciaService persistenciaService;

    public PartidaImportacaoService(
            ApiFootballClient apiFootballClient,
            PartidaPersistenciaService persistenciaService
    ) {
        this.apiFootballClient = apiFootballClient;
        this.persistenciaService = persistenciaService;
    }

    public ResultadoImportacaoPartidas importar(Long ligaId, Integer temporada) {
        ApiFixturesResponse resposta = apiFootballClient.buscarPartidas(ligaId, temporada);

        if (resposta == null) {
            throw new IllegalStateException("A API-Football retornou uma resposta vazia.");
        }

        if (resposta.possuiErros()) {
            throw new IllegalStateException("A API-Football retornou erros: " + resposta.errors());
        }

        return persistenciaService.salvar(resposta.response());
    }
}
