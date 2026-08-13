package com.meuprojeto.brasileirao;

import com.meuprojeto.brasileirao.repository.PartidaRepository;
import com.meuprojeto.brasileirao.repository.TimeRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("demo")
class DemoProfileTest {

    @Autowired
    private PartidaRepository partidaRepository;

    @Autowired
    private TimeRepository timeRepository;

    @Test
    void deveCriarCampeonatoDemonstrativoCompleto() {
        assertThat(timeRepository.count()).isEqualTo(20);
        assertThat(partidaRepository.count()).isEqualTo(380);
        assertThat(partidaRepository.findTemporadasByLigaId(71L))
                .containsExactly(2024);

        for (int rodada = 1; rodada <= 38; rodada++) {
            assertThat(partidaRepository
                    .findByLigaIdAndTemporadaAndRodadaOrderByDataHoraAsc(
                            71L,
                            2024,
                            rodada
                    ))
                    .hasSize(10);
        }
    }
}
