package com.senai.gestao_beneficios.repository;

import com.senai.gestao_beneficios.domain.solicitacao.Solicitacao;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface PageSolicitacaoRepository extends PagingAndSortingRepository<Solicitacao, String> {
}
