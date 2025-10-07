package com.senai.gestao_beneficios.DTO.medico;

import java.time.Instant;

public record MedicoAvaiabilityDTO (
        Instant horario,
        Boolean disponivel
){
}
