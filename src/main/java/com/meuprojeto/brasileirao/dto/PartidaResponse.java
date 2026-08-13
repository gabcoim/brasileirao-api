package com.meuprojeto.brasileirao.dto;

import com.meuprojeto.brasileirao.model.Partida;
import com.meuprojeto.brasileirao.model.StatusPartida;
import com.meuprojeto.brasileirao.model.Time;

import java.time.OffsetDateTime;

public record PartidaResponse(
        Long idExterno,
        Long ligaId,
        Integer temporada,
        Integer rodada,
        OffsetDateTime dataHora,
        StatusPartida status,
        Integer placarMandante,
        Integer placarVisitante,
        TimeResumo mandante,
        TimeResumo visitante
) {

    public static PartidaResponse from(Partida partida) {
        return new PartidaResponse(
                partida.getIdExterno(),
                partida.getLigaId(),
                partida.getTemporada(),
                partida.getRodada(),
                partida.getDataHora(),
                partida.getStatus(),
                partida.getPlacarMandante(),
                partida.getPlacarVisitante(),
                TimeResumo.from(partida.getMandante()),
                TimeResumo.from(partida.getVisitante())
        );
    }

    public record TimeResumo(
            Long idExterno,
            String nome,
            String sigla,
            String escudoUrl
    ) {

        public static TimeResumo from(Time time) {
            return new TimeResumo(
                    time.getIdExterno(),
                    time.getNome(),
                    time.getSigla(),
                    time.getEscudoUrl()
            );
        }
    }
}