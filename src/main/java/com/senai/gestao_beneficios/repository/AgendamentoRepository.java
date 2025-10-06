package com.senai.gestao_beneficios.repository;

import com.senai.gestao_beneficios.domain.agendamento.Agendamento;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AgendamentoRepository extends JpaRepository<Agendamento, String> {
}
