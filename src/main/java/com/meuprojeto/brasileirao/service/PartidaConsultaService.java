package com.meuprojeto.brasileirao.service;

import com.meuprojeto.brasileirao.dto.PartidaResponse;
import com.meuprojeto.brasileirao.repository.PartidaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PartidaConsultaService {

    private final PartidaRepository partidaRepository;

    public PartidaConsultaService(PartidaRepository partidaRepository) {
        this.partidaRepository = partidaRepository;
    }

    @Transactional(readOnly = true)
    public List<PartidaResponse> listar(
            Long ligaId,
            Integer temporada
    ) {
        return partidaRepository
                .findByLigaIdAndTemporadaOrderByRodadaAscDataHoraAsc(
                        ligaId,
                        temporada
                )
                .stream()
                .map(PartidaResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<Integer> listarTemporadas(Long ligaId) {
        return partidaRepository.findTemporadasByLigaId(ligaId);
    }

    @Transactional(readOnly = true)
    public List<PartidaResponse> buscarPorTemporadaERodada(
            Long ligaId,
            Integer temporada,
            Integer rodada
    ) {
        return partidaRepository
                .findByLigaIdAndTemporadaAndRodadaOrderByDataHoraAsc(
                        ligaId,
                        temporada,
                        rodada
                )
                .stream()
                .map(PartidaResponse::from)
                .toList();
    }
}
