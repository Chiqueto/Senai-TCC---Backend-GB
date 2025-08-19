package com.senai.gestao_beneficios.DTO.reponsePattern;


public record Pagination(
        int page,
        int size,
        long totalElements,
        int totalPages,
        String sort,
        boolean first,
        boolean last,
        boolean hasNext,
        boolean hasPrevious
) {
}
