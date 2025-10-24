package com.senai.gestao_beneficios.repository;

import com.senai.gestao_beneficios.domain.agendamento.Agendamento;
import com.senai.gestao_beneficios.domain.solicitacao.Solicitacao;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface PageAgendamentoRepository extends PagingAndSortingRepository<Agendamento, String>, JpaSpecificationExecutor<Agendamento> {
}
