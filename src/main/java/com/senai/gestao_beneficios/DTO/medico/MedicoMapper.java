package com.senai.gestao_beneficios.DTO.medico;

import com.senai.gestao_beneficios.DTO.disponibilidade.DisponibilidadeResponseDTO;
import com.senai.gestao_beneficios.DTO.especialidade.EspecialidadeDTO;
import com.senai.gestao_beneficios.domain.medico.Medico;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class MedicoMapper {
    public MedicoResponseDTO toDTO(Medico medico) {
        if (medico == null) {
            return null;
        }
        List<DisponibilidadeResponseDTO> disponibilidadeDTOs;
        if (medico.getDisponibilidade() == null) {
            disponibilidadeDTOs = Collections.emptyList(); // Trata caso de lista nula
        } else {
            disponibilidadeDTOs = medico.getDisponibilidade().stream()
                    .map(disp -> new DisponibilidadeResponseDTO(disp.getId(), disp.getDiaSemana()))
                    .collect(Collectors.toList());
        }

        EspecialidadeDTO especialidadeDTO = new EspecialidadeDTO(medico.getEspecialidade().getId(), medico.getEspecialidade().getNome());

        return new MedicoResponseDTO(
                medico.getId(),
                medico.getNome(),
                medico.getEmail(),
                especialidadeDTO,
                disponibilidadeDTOs,
                medico.getHoraEntrada(),
                medico.getHoraPausa(),
                medico.getHoraVolta(),
                medico.getHoraSaida()
        );
    }

    public List<MedicoResponseDTO> toDTOList(List<Medico> medicos) {
        return medicos.stream().map(this::toDTO).collect(Collectors.toList());
    }

    public MedicoResumeDTO toResumeDTO(Medico medico) {
        if (medico == null) {
            return null;
        }

        return new MedicoResumeDTO(
                medico.getId(),
                medico.getNome()
        );
    }

}
