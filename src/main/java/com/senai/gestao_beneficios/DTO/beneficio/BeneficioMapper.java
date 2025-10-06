package com.senai.gestao_beneficios.DTO.beneficio;

import com.senai.gestao_beneficios.domain.beneficio.Beneficio;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class BeneficioMapper {

    public BeneficioResponseDTO toDTO(Beneficio beneficio) {
        if (beneficio == null) {
            return null;
        }

        return new BeneficioResponseDTO(
                beneficio.getId(),
                beneficio.getNome(),
                beneficio.getDescricao(),
                beneficio.getPercentualDesconto()
        );
    }

    public List<BeneficioResponseDTO> toDTOList(List<Beneficio> beneficios) {
        return beneficios.stream().map(this::toDTO).collect(Collectors.toList());
    }
}
