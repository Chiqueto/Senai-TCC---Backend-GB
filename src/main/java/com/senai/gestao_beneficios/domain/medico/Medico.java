package com.senai.gestao_beneficios.domain.medico;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Entity
@Table(name = "medico", uniqueConstraints = @UniqueConstraint(columnNames = {"email"}))
@RequiredArgsConstructor
@Getter
@Setter
public class Medico {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    @Column(nullable = false)
    private String nome;
    @Column(nullable = false)
    private String email;
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
    @CreatedDate
    private Instant created_at;
    @LastModifiedDate
    private Instant updated_at;
}
