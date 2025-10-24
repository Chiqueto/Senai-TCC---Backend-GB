package com.senai.gestao_beneficios.DTO.medico;

import com.senai.gestao_beneficios.DTO.especialidade.EspecialidadeDTO;

public record MedicoResumeDTO(
        String id,
        String nome,
        EspecialidadeDTO especialidade
) {
}
