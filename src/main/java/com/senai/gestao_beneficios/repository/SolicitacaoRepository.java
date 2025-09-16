package com.senai.gestao_beneficios.repository;

import com.senai.gestao_beneficios.domain.solicitacao.Solicitacao;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SolicitacaoRepository extends JpaRepository<Solicitacao, String> {
}
