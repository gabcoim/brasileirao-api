package com.meuprojeto.brasileirao.dto;

public record ResultadoImportacaoPartidas(
        int recebidas,
        int criadas,
        int atualizadas,
        int ignoradas
) {
}
