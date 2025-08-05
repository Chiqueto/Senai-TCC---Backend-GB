package com.senai.gestao_beneficios.DTO.reponsePattern;

import java.util.Optional;

public record ApiMeta(
        Optional<Pagination> pagination
) {
}
