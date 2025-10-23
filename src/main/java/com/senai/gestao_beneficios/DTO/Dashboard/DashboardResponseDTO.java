package com.senai.gestao_beneficios.DTO.Dashboard;

import java.util.List;

public record DashboardResponseDTO(
        KpiDTO kpis,
        List<ConsultasPorMesDTO> consultasPorMes,
        List<SolicitacoesPorBeneficioDTO> solicitacoesPorBeneficio
) {}
