package com.senai.gestao_beneficios.DTO.agendamento;

import com.senai.gestao_beneficios.DTO.colaborador.ColaboradorMapper;
import com.senai.gestao_beneficios.DTO.dependente.DependenteDTO;
import com.senai.gestao_beneficios.DTO.dependente.DependenteMapper;
import com.senai.gestao_beneficios.DTO.medico.MedicoMapper;
import com.senai.gestao_beneficios.DTO.colaborador.ColaboradorDTO;
import com.senai.gestao_beneficios.DTO.medico.MedicoResponseDTO;
import com.senai.gestao_beneficios.domain.agendamento.Agendamento;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor // Anotação do Lombok para criar o construtor com os campos 'final'
public class AgendamentoMapper {

    // Injeção de dependência dos outros mappers necessários
    private final ColaboradorMapper colaboradorMapper;
    private final DependenteMapper dependenteMapper;
    private final MedicoMapper medicoMapper;

    public AgendamentoResponseDTO toDTO(Agendamento agendamento) {
        if (agendamento == null) {
            return null;
        }

        ColaboradorDTO colaboradorDTO = colaboradorMapper.toDTO(agendamento.getColaborador());
        MedicoResponseDTO medicoDTO = medicoMapper.toDTO(agendamento.getMedico());

        DependenteDTO dependenteDTO = agendamento.getDependente() != null ?
                dependenteMapper.toDTO(agendamento.getDependente()) : null;


        return new AgendamentoResponseDTO(
                agendamento.getId(),
                colaboradorDTO,
                dependenteDTO,
                medicoDTO,
                agendamento.getHorario(),
                agendamento.getStatus()
        );
    }

    public List<AgendamentoResponseDTO> toDTOList(List<Agendamento> agendamentos) {
        return agendamentos.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
}