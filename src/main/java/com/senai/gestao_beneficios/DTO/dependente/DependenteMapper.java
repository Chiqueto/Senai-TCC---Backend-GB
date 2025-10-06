package com.senai.gestao_beneficios.DTO.dependente;

import com.senai.gestao_beneficios.domain.dependente.Dependente;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class DependenteMapper {

    public DependenteDTO toDTO(Dependente dependente) {
        if (dependente == null) {
            return null;
        }

        return new DependenteDTO(
                dependente.getId(),
                dependente.getNome()
        );
    }

    public List<DependenteDTO> toDTOList(List<Dependente> dependentes) {
        return dependentes.stream().map(this::toDTO).collect(Collectors.toList());
    }
}