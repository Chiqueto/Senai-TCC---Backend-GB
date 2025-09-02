package com.senai.gestao_beneficios.DTO.medico;

import jakarta.validation.constraints.*;

import java.time.LocalTime;
import java.util.List;

public record MedicoRequestDTO(
        @NotNull(message = "Nome não pode ser nulo")
        @NotBlank(message = "Nome não pode ser vazio")
        String nome,
        @Email(message = "Email não está no formato correto")
        @NotBlank(message = "Email não pode ser vazio")
        @NotNull(message = "Email não pode ser nulo")
        String email,
        @NotNull(message = "Especialidade não pode ser nula")
        String id_especialidade,
        @NotNull(message = "Disponibilidade não pode ser nula")
        List<Integer> disponibilidade,
        @NotNull(message = "Hora de entrada não pode ser nula")
        LocalTime horaEntrada,
        @NotNull(message = "Hora de pausa não pode ser nula")
        LocalTime horaPausa,
        @NotNull(message = "Hora de volta não pode ser nula")
        LocalTime horaVolta,
        @NotNull(message = "Hora de saída não pode ser nula")
        LocalTime horaSaida
) {
}
