package com.senai.gestao_beneficios.DTO.especialidade;

import jakarta.validation.constraints.NotBlank;

public record EspecialidadeRequestDTO (
        @NotBlank(message = "nome não pode estar vazio!")
        String nome
){
}
