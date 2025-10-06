package com.senai.gestao_beneficios.service.agendamento;

import com.senai.gestao_beneficios.DTO.agendamento.AgendamentoMapper;
import com.senai.gestao_beneficios.DTO.agendamento.AgendamentoRequestDTO;
import com.senai.gestao_beneficios.DTO.agendamento.AgendamentoResponseDTO;
import com.senai.gestao_beneficios.DTO.reponsePattern.ApiResponse;
import com.senai.gestao_beneficios.domain.agendamento.Agendamento;
import com.senai.gestao_beneficios.domain.agendamento.StatusAgendamento;
import com.senai.gestao_beneficios.domain.colaborador.Colaborador;
import com.senai.gestao_beneficios.domain.dependente.Dependente;
import com.senai.gestao_beneficios.domain.medico.Medico;
import com.senai.gestao_beneficios.infra.exceptions.NotFoundException;
import com.senai.gestao_beneficios.repository.AgendamentoRepository;
import com.senai.gestao_beneficios.repository.ColaboradorRepository;
import com.senai.gestao_beneficios.repository.DependenteRepository;
import com.senai.gestao_beneficios.repository.MedicoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AgendamentoService {
    final ColaboradorRepository colaboradorRepository;
    final DependenteRepository dependenteRepository;
    final MedicoRepository medicoRepository;
    final AgendamentoRepository agendamentoRepository;
    final AgendamentoMapper mapper;


    public ApiResponse<AgendamentoResponseDTO> criarAgendamento (AgendamentoRequestDTO requestDTO){
        Colaborador colaborador = colaboradorRepository.findById(requestDTO.idColaborador()).orElseThrow(() -> new NotFoundException("colaborador", "colaborador não encontrado"));

        Dependente dependente = null;
        if(requestDTO.idDependente() != null){
            dependente = dependenteRepository.findByIdAndColaboradorId(requestDTO.idDependente(), requestDTO.idColaborador()).orElseThrow(() -> new NotFoundException("dependente", "Dependente não encontrado para esse colaborador"));
        }

        Medico medico = medicoRepository.findById(requestDTO.idMedico()).orElseThrow(() -> new NotFoundException("medico", "Médico não encontrado"));

        Agendamento agendamentoToSave = new Agendamento();
        agendamentoToSave.setColaborador(colaborador);
        agendamentoToSave.setDependente(dependente);
        agendamentoToSave.setMedico(medico);
        agendamentoToSave.setHorario(requestDTO.hoario());
        agendamentoToSave.setStatus(StatusAgendamento.AGENDADO);
        Agendamento agendamento = agendamentoRepository.save(agendamentoToSave);

        AgendamentoResponseDTO response = mapper.toDTO(agendamento);

        return new ApiResponse<AgendamentoResponseDTO>(true, response, null, null, "Agendamento criado com sucesso!");
    }

}
