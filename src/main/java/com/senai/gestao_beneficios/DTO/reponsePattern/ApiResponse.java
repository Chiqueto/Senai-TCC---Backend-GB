package com.senai.gestao_beneficios.DTO.reponsePattern;

public record ApiResponse<T>(
        boolean success,
        T data,
        ApiError error,
        ApiMeta meta
) {
}
