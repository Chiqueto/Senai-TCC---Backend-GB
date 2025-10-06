package com.senai.gestao_beneficios.DTO.agendamento;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public record AgendamentoRequestDTO(
        @NotBlank(message = "Colaborador deve ser informado!")
        String idColaborador,
        @NotBlank(message = "Medico deve ser informado!")
        String idMedico,
        String idDependente,
        @NotNull(message = "Horário deve ser informado!")
        Instant hoario
) {
}
