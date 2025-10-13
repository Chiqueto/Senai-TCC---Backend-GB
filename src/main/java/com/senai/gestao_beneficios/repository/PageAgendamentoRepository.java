package com.senai.gestao_beneficios.repository;

import com.senai.gestao_beneficios.domain.agendamento.Agendamento;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface PageAgendamentoRepository extends PagingAndSortingRepository<Agendamento, String> {
}
