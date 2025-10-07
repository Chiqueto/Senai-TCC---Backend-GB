package com.senai.gestao_beneficios.repository;

import com.senai.gestao_beneficios.domain.agendamento.Agendamento;
import com.senai.gestao_beneficios.domain.agendamento.StatusAgendamento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;

public interface AgendamentoRepository extends JpaRepository<Agendamento, String> {
    boolean existsByMedicoIdAndHorarioAndStatusNot(String medicoId, Instant horario, StatusAgendamento status);
}
