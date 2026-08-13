package com.meuprojeto.brasileirao.controller;

import com.meuprojeto.brasileirao.dto.ResultadoImportacaoPartidas;
import com.meuprojeto.brasileirao.service.PartidaImportacaoService;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import org.springframework.context.annotation.Profile;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@Profile("!demo")
@RequestMapping("/importacoes")
public class ImportacaoController {

    private final PartidaImportacaoService partidaImportacaoService;

    public ImportacaoController(PartidaImportacaoService partidaImportacaoService) {
        this.partidaImportacaoService = partidaImportacaoService;
    }

    @PostMapping("/partidas")
    public ResultadoImportacaoPartidas importarPartidas(
            @RequestParam(defaultValue = "71") @Positive Long ligaId,
            @RequestParam @Min(1900) Integer temporada
    ) {
        return partidaImportacaoService.importar(ligaId, temporada);
    }
}
