package com.senai.gestao_beneficios.domain.agendamento;

import com.senai.gestao_beneficios.domain.colaborador.Colaborador;
import com.senai.gestao_beneficios.domain.dependente.Dependente;
import com.senai.gestao_beneficios.domain.medico.Medico;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.annotation.CreatedDate;

import java.time.Instant;

@Entity
@Table(name = "agendamentos")
@Getter
@Setter
@RequiredArgsConstructor
public class Agendamento {
    @Id
    @GeneratedValue (strategy = GenerationType.UUID)
    private String id;
    @ManyToOne
    private Colaborador colaborador;
    @ManyToOne
    private Dependente dependente;
    @ManyToOne
    private Medico medico;
    @Column(nullable = false)
    private Instant horario;
    @Column(nullable = false)
    private StatusAgendamento status;
    @CreatedDate
    @Column(updatable = false)
    public Instant created_at = Instant.now();
    @UpdateTimestamp
    public Instant updated_at = Instant.now();
}
