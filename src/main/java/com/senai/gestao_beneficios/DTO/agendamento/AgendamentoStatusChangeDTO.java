package com.senai.gestao_beneficios.DTO.agendamento;

import com.senai.gestao_beneficios.domain.agendamento.StatusAgendamento;
import jakarta.validation.constraints.NotNull;

public record AgendamentoStatusChangeDTO(
        @NotNull(message = "Novo status deve ser informado")
        StatusAgendamento status
) {
}
