package com.meuprojeto.brasileirao.controller;

import com.meuprojeto.brasileirao.dto.PartidaResponse;
import com.meuprojeto.brasileirao.service.PartidaConsultaService;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Validated
@RestController
@RequestMapping("/partidas")
public class PartidaController {

    private final PartidaConsultaService partidaConsultaService;

    public PartidaController(PartidaConsultaService partidaConsultaService) {
        this.partidaConsultaService = partidaConsultaService;
    }

    @GetMapping
    public List<PartidaResponse> listar(
            @RequestParam(defaultValue = "71") @Positive Long ligaId,
            @RequestParam @Min(1900) Integer temporada
    ) {
        return partidaConsultaService.listar(ligaId, temporada);
    }

    @GetMapping("/temporadas")
    public List<Integer> listarTemporadas(
            @RequestParam(defaultValue = "71") @Positive Long ligaId
    ) {
        return partidaConsultaService.listarTemporadas(ligaId);
    }

    @GetMapping("/temporada/{temporada}/rodada/{rodada}")
    public List<PartidaResponse> buscarPorTemporadaERodada(
            @PathVariable @Min(1900) Integer temporada,
            @PathVariable @Positive Integer rodada,
            @RequestParam(defaultValue = "71") @Positive Long ligaId
    ) {
        return partidaConsultaService.buscarPorTemporadaERodada(
                ligaId,
                temporada,
                rodada
        );
    }
}
