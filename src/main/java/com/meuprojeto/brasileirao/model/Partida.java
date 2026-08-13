package com.meuprojeto.brasileirao.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

@Entity
@Table(name = "partidas")
@Getter
@Setter
@NoArgsConstructor
public class Partida {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private Long idExterno;

    @Column(name = "liga_id")
    private Long ligaId;

    private Integer temporada;

    @ManyToOne
    @JoinColumn(name = "time_mandante_id")
    private Time mandante;

    @ManyToOne
    @JoinColumn(name = "time_visitante_id")
    private Time visitante;

    private Integer placarMandante;
    private Integer placarVisitante;

    private OffsetDateTime dataHora;

    private Integer rodada;

    @Enumerated(EnumType.STRING)
    private StatusPartida status;
}
