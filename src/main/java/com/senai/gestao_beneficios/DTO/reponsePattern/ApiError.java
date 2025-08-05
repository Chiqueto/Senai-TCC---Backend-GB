package com.senai.gestao_beneficios.DTO.reponsePattern;

public record ApiError(
        String code,
        String message
) {
}
