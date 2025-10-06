package com.senai.gestao_beneficios.DTO.beneficio;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record BeneficioRequestDTO(
        @NotNull
        @NotBlank
        String nome,
        String descricao,
        @NotNull
        Integer percentualDesconto
) {
}
