package com.meuprojeto.brasileirao.config;

import com.meuprojeto.brasileirao.model.Partida;
import com.meuprojeto.brasileirao.model.StatusPartida;
import com.meuprojeto.brasileirao.model.Time;
import com.meuprojeto.brasileirao.repository.PartidaRepository;
import com.meuprojeto.brasileirao.repository.TimeRepository;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

@Configuration
@Profile("demo")
public class DemoDataConfig {

    private static final long LIGA_ID = 71L;
    private static final int TEMPORADA = 2024;
    private static final long PRIMEIRO_ID_EXTERNO_PARTIDA = 9_000_000L;

    @Bean
    public ApplicationRunner carregarDadosDemonstrativos(
            TimeRepository timeRepository,
            PartidaRepository partidaRepository
    ) {
        return argumentos -> {
            if (partidaRepository.count() > 0) {
                return;
            }

            List<Time> times = criarTimes(timeRepository);
            criarCampeonatoCompleto(times, partidaRepository);
        };
    }

    private List<Time> criarTimes(TimeRepository timeRepository) {
        List<String> nomes = List.of(
                "Atlético Aurora",
                "Bandeirantes FC",
                "Clube do Cerrado",
                "Desportivo Nacional",
                "Estrela do Norte",
                "Ferroviário do Sul",
                "Grêmio Litorâneo",
                "Horizonte EC",
                "Independente Paulista",
                "Juventude da Serra",
                "Lagoa Azul FC",
                "Metropolitano Mineiro",
                "Náutico do Oeste",
                "Operário Central",
                "Pioneiros FC",
                "Real Nordeste",
                "Sporting Capital",
                "União Carioca",
                "Vila Imperial",
                "XV do Vale"
        );

        List<Time> times = new ArrayList<>();

        for (int indice = 0; indice < nomes.size(); indice++) {
            Time time = new Time();
            time.setIdExterno(90_001L + indice);
            time.setNome(nomes.get(indice));
            time.setSigla(criarSigla(nomes.get(indice)));
            times.add(timeRepository.save(time));
        }

        return times;
    }

    private void criarCampeonatoCompleto(
            List<Time> times,
            PartidaRepository partidaRepository
    ) {
        List<Time> ordemRodadas = new ArrayList<>(times);
        List<Confronto> confrontosDoPrimeiroTurno = new ArrayList<>();
        long proximoIdExterno = PRIMEIRO_ID_EXTERNO_PARTIDA;

        for (int rodada = 1; rodada <= 19; rodada++) {
            for (int indice = 0; indice < ordemRodadas.size() / 2; indice++) {
                Time primeiro = ordemRodadas.get(indice);
                Time segundo = ordemRodadas.get(ordemRodadas.size() - 1 - indice);
                Time mandante = rodada % 2 == 0 ? segundo : primeiro;
                Time visitante = rodada % 2 == 0 ? primeiro : segundo;

                confrontosDoPrimeiroTurno.add(new Confronto(rodada, mandante, visitante));
                partidaRepository.save(criarPartida(
                        proximoIdExterno++, rodada, indice, mandante, visitante
                ));
            }

            girarTimesParaProximaRodada(ordemRodadas);
        }

        for (Confronto confronto : confrontosDoPrimeiroTurno) {
            int rodadaSegundoTurno = confronto.rodada() + 19;
            partidaRepository.save(criarPartida(
                    proximoIdExterno++,
                    rodadaSegundoTurno,
                    confronto.rodada(),
                    confronto.visitante(),
                    confronto.mandante()
            ));
        }
    }

    private Partida criarPartida(
            long idExterno,
            int rodada,
            int indiceDoJogo,
            Time mandante,
            Time visitante
    ) {
        Partida partida = new Partida();
        partida.setIdExterno(idExterno);
        partida.setLigaId(LIGA_ID);
        partida.setTemporada(TEMPORADA);
        partida.setMandante(mandante);
        partida.setVisitante(visitante);
        partida.setPlacarMandante(Math.floorMod(rodada + indiceDoJogo, 4));
        partida.setPlacarVisitante(Math.floorMod(rodada * 2 + indiceDoJogo, 3));
        partida.setDataHora(OffsetDateTime.of(
                        2024, 4, 13, 16, 0, 0, 0, ZoneOffset.ofHours(-3)
                )
                .plusWeeks(rodada - 1L)
                .plusHours(indiceDoJogo * 2L));
        partida.setRodada(rodada);
        partida.setStatus(StatusPartida.FINALIZADA);
        return partida;
    }

    private void girarTimesParaProximaRodada(List<Time> times) {
        Time ultimo = times.remove(times.size() - 1);
        times.add(1, ultimo);
    }

    private String criarSigla(String nome) {
        String[] palavras = nome
                .replaceAll("[^A-Za-zÀ-ÿ ]", "")
                .trim()
                .split("\\s+");
        StringBuilder sigla = new StringBuilder();

        for (int indice = 0; indice < Math.min(3, palavras.length); indice++) {
            sigla.append(palavras[indice].charAt(0));
        }

        return sigla.toString().toUpperCase();
    }

    private record Confronto(
            int rodada,
            Time mandante,
            Time visitante
    ) {
    }
}
