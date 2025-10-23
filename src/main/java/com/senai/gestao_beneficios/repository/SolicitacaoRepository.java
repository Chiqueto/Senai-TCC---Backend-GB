package com.senai.gestao_beneficios.repository;

import com.senai.gestao_beneficios.DTO.Dashboard.BeneficioCountDTO;
import com.senai.gestao_beneficios.domain.colaborador.Colaborador;
import com.senai.gestao_beneficios.domain.solicitacao.Solicitacao;
import com.senai.gestao_beneficios.domain.solicitacao.StatusSolicitacao;
import com.senai.gestao_beneficios.domain.solicitacao.TipoPagamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.util.List;
import java.util.Map;
import java.util.Optional;
public interface SolicitacaoRepository extends JpaRepository<Solicitacao, String> {
    List<Solicitacao> findByColaboradorId(String colaboradorId);

    Optional<Solicitacao> findByIdAndColaboradorId(String id, String colaboradorId);

    List<Solicitacao> findByColaboradorIdAndStatusAndTipoPagamento(
            String colaboradorId,
            StatusSolicitacao status,
            TipoPagamento tipoPagamento
    );

    long countByStatus(StatusSolicitacao status);

    @Query(value = "SELECT b.nome as beneficio, COUNT(s) as quantidade" +
            "   FROM solicitacao_beneficio s JOIN beneficio b ON b.id = s.beneficio_id\n" +
            "   GROUP BY b.nome", nativeQuery = true)
    List<BeneficioCountDTO> countSolicitacoesPorTipoBeneficio();
}
