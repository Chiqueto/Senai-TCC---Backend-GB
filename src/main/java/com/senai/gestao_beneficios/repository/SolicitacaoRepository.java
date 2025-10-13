package com.senai.gestao_beneficios.repository;

import com.senai.gestao_beneficios.domain.colaborador.Colaborador;
import com.senai.gestao_beneficios.domain.solicitacao.Solicitacao;
import com.senai.gestao_beneficios.domain.solicitacao.StatusSolicitacao;
import com.senai.gestao_beneficios.domain.solicitacao.TipoPagamento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SolicitacaoRepository extends JpaRepository<Solicitacao, String> {
    List<Solicitacao> findByColaboradorId(String colaboradorId);

    Optional<Solicitacao> findByIdAndColaboradorId(String id, String colaboradorId);

    List<Solicitacao> findByColaboradorIdAndStatusAndTipoPagamento(
            String colaboradorId,
            StatusSolicitacao status,
            TipoPagamento tipoPagamento
    );
}
