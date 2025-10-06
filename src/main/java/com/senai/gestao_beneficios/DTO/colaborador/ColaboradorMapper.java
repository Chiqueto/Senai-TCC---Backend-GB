package com.senai.gestao_beneficios.DTO.colaborador;

import com.senai.gestao_beneficios.DTO.dependente.DependenteDTO;
import com.senai.gestao_beneficios.DTO.dependente.DependenteMapper;
import com.senai.gestao_beneficios.domain.colaborador.Colaborador;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor // Injeta o DependenteMapper no construtor
public class ColaboradorMapper {

    // Dependência para o mapper de dependentes
    private final DependenteMapper dependenteMapper;

    public ColaboradorDTO toDTO(Colaborador colaborador) {
        if (colaborador == null) {
            return null;
        }

        // --- Lógica para converter a lista de dependentes ---
        Set<DependenteDTO> dependentesDTOs;
        if (colaborador.getDependentes() == null || colaborador.getDependentes().isEmpty()) {
            dependentesDTOs = Collections.emptySet(); // Retorna um conjunto vazio se não houver dependentes
        } else {
            dependentesDTOs = colaborador.getDependentes().stream()
                    // Reutiliza o DependenteMapper para converter cada dependente
                    .map(dependenteMapper::toDTO)
                    .collect(Collectors.toSet());
        }

        return new ColaboradorDTO(
                colaborador.getId(),
                colaborador.getNome(),
                colaborador.getMatricula(),
                colaborador.getDtNascimento(),
                colaborador.getFuncao(),
                colaborador.getGenero(),
                colaborador.getCidade(),
                dependentesDTOs // Usa a lista de DTOs convertida
        );
    }

    public List<ColaboradorDTO> toDTOList(List<Colaborador> colaboradores) {
        return colaboradores.stream().map(this::toDTO).collect(Collectors.toList());
    }
}