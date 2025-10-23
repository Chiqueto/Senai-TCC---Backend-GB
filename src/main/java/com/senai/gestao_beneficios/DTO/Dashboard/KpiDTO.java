package com.senai.gestao_beneficios.DTO.Dashboard;

public record KpiDTO(
        long solicitacoesPendentes,
        long solicitacoesRecusadas,
        long solicitacoesPendenteAssinatura,
        long consultasPendentes,
        long consultasDoMes
) {}
