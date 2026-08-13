package com.meuprojeto.brasileirao.repository;

import com.meuprojeto.brasileirao.model.Partida;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PartidaRepository
        extends JpaRepository<Partida, Long> {

    Optional<Partida> findByIdExterno(Long idExterno);

    @EntityGraph(attributePaths = {"mandante", "visitante"})
    List<Partida> findByLigaIdAndTemporadaOrderByRodadaAscDataHoraAsc(
            Long ligaId,
            Integer temporada
    );

    @EntityGraph(attributePaths = {"mandante", "visitante"})
    List<Partida> findByLigaIdAndTemporadaAndRodadaOrderByDataHoraAsc(
            Long ligaId,
            Integer temporada,
            Integer rodada
    );

    @Query("""
            select distinct p.temporada
            from Partida p
            where p.ligaId = :ligaId
              and p.temporada is not null
            order by p.temporada desc
            """)
    List<Integer> findTemporadasByLigaId(@Param("ligaId") Long ligaId);
}
