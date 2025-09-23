package com.senai.gestao_beneficios.DTO.solicitacao;

import com.senai.gestao_beneficios.DTO.beneficio.BeneficioResponseDTO;
import com.senai.gestao_beneficios.DTO.colaborador.ColaboradorDTO;
import com.senai.gestao_beneficios.DTO.dependente.DependenteDTO;
import com.senai.gestao_beneficios.domain.solicitacao.StatusSolicitacao;
import com.senai.gestao_beneficios.domain.solicitacao.TipoPagamento;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.Instant;

public record SolicitacaoRequestDTO(
        @NotBlank(message = "O colaborador é obrigatório.")
        String idColaborador,
        @NotBlank(message = "O benefício é obrigatório.")
        String idBeneficio,
        @Min(value = 1, message = "O valor total deve ser maior que zero.")
        @NotNull(message = "O valor total é obrigatório.")
        BigDecimal valorTotal,
        String idDependente,
        BigDecimal desconto,
        String descricao,
        Integer qtdeParcelas,
        TipoPagamento tipoPagamento
) {
}
