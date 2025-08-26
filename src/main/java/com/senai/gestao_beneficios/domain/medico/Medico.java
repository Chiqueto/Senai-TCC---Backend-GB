package com.senai.gestao_beneficios.domain.medico;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Entity
@Table(name = "medico")
@RequiredArgsConstructor
@Getter
@Setter
public class Medico {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    @Column(nullable = false)
    private String nome;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "especialidade_id", nullable = false)
    private Especialidade especialidade;
    @OneToMany(mappedBy = "medico")
    private List<Disponibilidade> disponibilidade;
    @Column(nullable = false)
    private LocalTime horaEntrada;
    @Column(nullable = false)
    private LocalTime horaPausa;
    @Column(nullable = false)
    private LocalTime horaVolta;
    @Column(nullable = false)
    private LocalTime horaSaida;
    private Instant created_at;
    private Instant updated_at;
}
