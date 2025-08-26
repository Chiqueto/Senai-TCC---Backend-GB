package com.senai.gestao_beneficios.DTO.medico;

import com.senai.gestao_beneficios.domain.medico.Disponibilidade;
import com.senai.gestao_beneficios.domain.medico.Especialidade;
import jakarta.persistence.*;

import java.time.LocalTime;
import java.util.List;

public record MedicoRequestDTO(
        String nome,
        Especialidade especialidade,
        List<Disponibilidade> disponibilidade,
        LocalTime horaEntrada,
        LocalTime horaPausa,
        LocalTime horaVolta,
        LocalTime horaSaida
) {
}
