package com.senai.gestao_beneficios.DTO.solicitacao;

import com.senai.gestao_beneficios.DTO.beneficio.BeneficioResponseDTO;
import com.senai.gestao_beneficios.DTO.colaborador.ColaboradorDTO;
import com.senai.gestao_beneficios.DTO.dependente.DependenteDTO;
import com.senai.gestao_beneficios.domain.beneficio.Beneficio;
import com.senai.gestao_beneficios.domain.colaborador.Colaborador;
import com.senai.gestao_beneficios.domain.dependente.Dependente;
import com.senai.gestao_beneficios.domain.solicitacao.StatusSolicitacao;
import com.senai.gestao_beneficios.domain.solicitacao.TipoPagamento;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

import java.math.BigDecimal;
import java.time.Instant;

public record SolicitacaoResponseDTO (

        String id,
        ColaboradorDTO colaborador,
        DependenteDTO dependente,
        BeneficioResponseDTO beneficio,
        BigDecimal valorTotal,
        BigDecimal desconto,
        String descricao,
        Integer qtdeParcelas,
        Instant dataSolicitacao,
        TipoPagamento tipoPagamento,
        StatusSolicitacao status
) {
}
