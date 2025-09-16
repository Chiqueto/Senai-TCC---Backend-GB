package com.senai.gestao_beneficios.DTO.dependente;

import com.senai.gestao_beneficios.domain.colaborador.Funcao;
import com.senai.gestao_beneficios.domain.colaborador.Genero;
import com.senai.gestao_beneficios.domain.dependente.Dependente;

import java.time.LocalDate;
import java.util.Set;

public record DependenteDTO(
        String id,
        String nome
) {
}
