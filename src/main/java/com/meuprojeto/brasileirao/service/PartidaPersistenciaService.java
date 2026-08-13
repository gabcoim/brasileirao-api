package com.meuprojeto.brasileirao.service;

import com.meuprojeto.brasileirao.dto.ResultadoImportacaoPartidas;
import com.meuprojeto.brasileirao.dto.apifootball.ApiFixtureItem;
import com.meuprojeto.brasileirao.dto.apifootball.ApiTeam;
import com.meuprojeto.brasileirao.model.Partida;
import com.meuprojeto.brasileirao.model.StatusPartida;
import com.meuprojeto.brasileirao.model.Time;
import com.meuprojeto.brasileirao.repository.PartidaRepository;
import com.meuprojeto.brasileirao.repository.TimeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class PartidaPersistenciaService {

    private static final Pattern ULTIMO_NUMERO = Pattern.compile("(\\d+)(?!.*\\d)");

    private final PartidaRepository partidaRepository;
    private final TimeRepository timeRepository;

    public PartidaPersistenciaService(
            PartidaRepository partidaRepository,
            TimeRepository timeRepository
    ) {
        this.partidaRepository = partidaRepository;
        this.timeRepository = timeRepository;
    }

    @Transactional
    public ResultadoImportacaoPartidas salvar(List<ApiFixtureItem> itens) {
        int criadas = 0;
        int atualizadas = 0;
        int ignoradas = 0;
        Map<Long, Time> timesProcessados = new HashMap<>();

        for (ApiFixtureItem item : itens) {
            if (!itemValido(item)) {
                ignoradas++;
                continue;
            }

            Time mandante = salvarOuAtualizarTime(item.teams().home(), timesProcessados);
            Time visitante = salvarOuAtualizarTime(item.teams().away(), timesProcessados);

            Optional<Partida> partidaExistente =
                    partidaRepository.findByIdExterno(item.fixture().id());
            Partida partida = partidaExistente.orElseGet(Partida::new);

            partida.setIdExterno(item.fixture().id());
            partida.setLigaId(item.league().id());
            partida.setTemporada(item.league().season());
            partida.setMandante(mandante);
            partida.setVisitante(visitante);
            partida.setDataHora(item.fixture().date());
            partida.setStatus(converterStatus(item.fixture().status().shortStatus()));
            partida.setRodada(extrairRodada(item.league().round()));

            if (item.goals() != null) {
                partida.setPlacarMandante(item.goals().home());
                partida.setPlacarVisitante(item.goals().away());
            }

            partidaRepository.save(partida);

            if (partidaExistente.isPresent()) {
                atualizadas++;
            } else {
                criadas++;
            }
        }

        return new ResultadoImportacaoPartidas(itens.size(), criadas, atualizadas, ignoradas);
    }

    private Time salvarOuAtualizarTime(ApiTeam timeApi, Map<Long, Time> timesProcessados) {
        Time timeJaProcessado = timesProcessados.get(timeApi.id());
        if (timeJaProcessado != null) {
            return timeJaProcessado;
        }

        Time time = timeRepository.findByIdExterno(timeApi.id()).orElseGet(Time::new);
        time.setIdExterno(timeApi.id());
        time.setNome(timeApi.name());
        time.setEscudoUrl(timeApi.logo());

        Time timeSalvo = timeRepository.save(time);
        timesProcessados.put(timeApi.id(), timeSalvo);
        return timeSalvo;
    }

    private boolean itemValido(ApiFixtureItem item) {
        return item != null
                && item.fixture() != null
                && item.fixture().id() != null
                && item.fixture().status() != null
                && item.teams() != null
                && timeValido(item.teams().home())
                && timeValido(item.teams().away())
                && item.league() != null;
    }

    private boolean timeValido(ApiTeam time) {
        return time != null && time.id() != null;
    }

    private StatusPartida converterStatus(String status) {
        if (status == null) {
            return StatusPartida.DESCONHECIDA;
        }

        return switch (status) {
            case "TBD", "NS" -> StatusPartida.AGENDADA;
            case "1H", "HT", "2H", "ET", "BT", "P", "LIVE" ->
                    StatusPartida.EM_ANDAMENTO;
            case "FT", "AET", "PEN", "AWD", "WO" -> StatusPartida.FINALIZADA;
            case "PST" -> StatusPartida.ADIADA;
            case "CANC" -> StatusPartida.CANCELADA;
            case "SUSP", "INT", "ABD" -> StatusPartida.INTERROMPIDA;
            default -> StatusPartida.DESCONHECIDA;
        };
    }

    private Integer extrairRodada(String descricaoRodada) {
        if (descricaoRodada == null) {
            return null;
        }

        Matcher matcher = ULTIMO_NUMERO.matcher(descricaoRodada);
        return matcher.find() ? Integer.valueOf(matcher.group(1)) : null;
    }
}
