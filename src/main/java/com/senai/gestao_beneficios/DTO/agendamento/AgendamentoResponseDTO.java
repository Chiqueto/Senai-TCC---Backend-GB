package com.senai.gestao_beneficios.DTO.agendamento;

import com.senai.gestao_beneficios.DTO.colaborador.ColaboradorDTO;
import com.senai.gestao_beneficios.DTO.dependente.DependenteDTO;
import com.senai.gestao_beneficios.DTO.medico.MedicoResponseDTO;
import com.senai.gestao_beneficios.domain.agendamento.StatusAgendamento;

import java.time.Instant;

public record AgendamentoResponseDTO(
        String idAgendamento,
        ColaboradorDTO colaborador,
        DependenteDTO dependente,
        MedicoResponseDTO medico,
        Instant horario,
        StatusAgendamento status

) {
}
