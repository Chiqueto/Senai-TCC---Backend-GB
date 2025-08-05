package com.senai.gestao_beneficios.DTO.reponsePattern;

import java.util.Optional;

public record Pagination(
        int currentPage,
        int pageSize,
        Optional<String> nextPage,
        Optional<String> previousPage,
        int total
) {
}
