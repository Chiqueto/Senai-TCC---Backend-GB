package com.senai.gestao_beneficios.DTO.beneficio;


public record BeneficioResponseDTO(
        String id,
        String nome,
        String descricao,
        Integer percentualDesconto
) {
}
