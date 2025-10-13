package com.senai.gestao_beneficios.DTO.colaborador;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ParcelaAbertaDTO(
        String idSolicitacao,
        String nomeBeneficio,
        String numeroParcela,
        BigDecimal valorParcela,
        LocalDate dataVencimento
) {
}