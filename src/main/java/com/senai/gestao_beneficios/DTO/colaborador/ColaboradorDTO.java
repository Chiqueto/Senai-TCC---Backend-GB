package com.senai.gestao_beneficios.DTO.colaborador;

import com.senai.gestao_beneficios.DTO.dependente.DependenteDTO;
import com.senai.gestao_beneficios.domain.colaborador.Funcao;
import com.senai.gestao_beneficios.domain.colaborador.Genero;
import com.senai.gestao_beneficios.domain.dependente.Dependente;

import java.time.LocalDate;
import java.util.Set;

public record ColaboradorDTO (
        String id,
        String nome,
        String matricula,
        LocalDate dtNascimento,
        Funcao funcao,
        Genero genero,
        String cidade,
        Set<DependenteDTO> dependentes
) {
}
