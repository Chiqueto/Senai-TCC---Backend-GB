package com.senai.gestao_beneficios.DTO.medico;

import com.senai.gestao_beneficios.DTO.disponibilidade.DisponibilidadeResponseDTO;
import com.senai.gestao_beneficios.DTO.especialidade.EspecialidadeDTO;
import com.senai.gestao_beneficios.domain.medico.Especialidade;

import java.time.LocalTime;
import java.util.List;

public record MedicoResponseDTO(
        String id,
        String nome,
        String email,
        EspecialidadeDTO especialidade,
        List<DisponibilidadeResponseDTO> disponibilidade,
        LocalTime horaEntrada,
        LocalTime horaPausa,
        LocalTime horaVolta,
        LocalTime horaSaida
) {
}
