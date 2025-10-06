package com.senai.gestao_beneficios.repository;

import com.senai.gestao_beneficios.domain.solicitacao.Solicitacao;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface PageSolicitacaoRepository extends PagingAndSortingRepository<Solicitacao, String> {
    Page<Solicitacao> findByColaboradorId(String colaboradorId, Pageable pageable);
}
