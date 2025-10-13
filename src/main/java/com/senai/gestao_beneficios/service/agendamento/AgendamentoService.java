package com.senai.gestao_beneficios.service.agendamento;

import com.senai.gestao_beneficios.DTO.agendamento.AgendamentoMapper;
import com.senai.gestao_beneficios.DTO.agendamento.AgendamentoRequestDTO;
import com.senai.gestao_beneficios.DTO.agendamento.AgendamentoResponseDTO;
import com.senai.gestao_beneficios.DTO.reponsePattern.ApiResponse;
import com.senai.gestao_beneficios.DTO.solicitacao.SolicitacaoResponseDTO;
import com.senai.gestao_beneficios.domain.agendamento.Agendamento;
import com.senai.gestao_beneficios.domain.agendamento.StatusAgendamento;
import com.senai.gestao_beneficios.domain.colaborador.Colaborador;
import com.senai.gestao_beneficios.domain.dependente.Dependente;
import com.senai.gestao_beneficios.domain.medico.Disponibilidade;
import com.senai.gestao_beneficios.domain.medico.Medico;
import com.senai.gestao_beneficios.domain.solicitacao.Solicitacao;
import com.senai.gestao_beneficios.infra.exceptions.BadRequest;
import com.senai.gestao_beneficios.infra.exceptions.ConflictException;
import com.senai.gestao_beneficios.infra.exceptions.NotFoundException;
import com.senai.gestao_beneficios.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.*;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AgendamentoService {
    final ColaboradorRepository colaboradorRepository;
    final DependenteRepository dependenteRepository;
    final MedicoRepository medicoRepository;
    final AgendamentoRepository agendamentoRepository;
    final AgendamentoMapper mapper;
    final DisponibilidadeRepository disponibilidadeRepository;
    final PageAgendamentoRepository pageAgendamentoRepository;
    private static final ZoneId FUSO_HORARIO_NEGOCIO = ZoneId.of("America/Sao_Paulo");

    public ApiResponse<AgendamentoResponseDTO> criarAgendamento (AgendamentoRequestDTO requestDTO){
        Colaborador colaborador = colaboradorRepository.findById(requestDTO.idColaborador()).orElseThrow(() -> new NotFoundException("colaborador", "colaborador não encontrado"));

        Dependente dependente = null;
        if(requestDTO.idDependente() != null){
            dependente = dependenteRepository.findByIdAndColaboradorId(requestDTO.idDependente(), requestDTO.idColaborador()).orElseThrow(() -> new NotFoundException("dependente", "Dependente não encontrado para esse colaborador"));
        }

        Medico medico = medicoRepository.findById(requestDTO.idMedico()).orElseThrow(() -> new NotFoundException("medico", "Médico não encontrado"));

        verificarDisponibilidade(requestDTO.idMedico(), requestDTO.horario());

        Agendamento agendamentoToSave = new Agendamento();
        agendamentoToSave.setColaborador(colaborador);
        agendamentoToSave.setDependente(dependente);
        agendamentoToSave.setMedico(medico);
        agendamentoToSave.setHorario(requestDTO.horario());
        agendamentoToSave.setStatus(StatusAgendamento.AGENDADO);
        Agendamento agendamento = agendamentoRepository.save(agendamentoToSave);

        AgendamentoResponseDTO response = mapper.toDTO(agendamento);


        return new ApiResponse<AgendamentoResponseDTO>(true, response, null, null, "Agendamento criado com sucesso!");
    }

    private void verificarDisponibilidade(String medicoId, Instant horarioSolicitado) {

        Medico medico = medicoRepository.findById(medicoId)
                .orElseThrow(() -> new NotFoundException("medico", "Médico não encontrado"));

        // Converte o horário UTC para o fuso horário do negócio para podermos comparar dia e hora
        ZonedDateTime agendamentoNoFuso = horarioSolicitado.atZone(FUSO_HORARIO_NEGOCIO);

        // --- ETAPA 1: O médico trabalha neste dia da semana? ---
        DayOfWeek diaDaSemanaSolicitado = agendamentoNoFuso.getDayOfWeek(); // Ex: MONDAY

        // Mapeia o dia da semana do Java (MONDAY=1, SUNDAY=7) para o seu padrão (domingo=0, segunda=1)
        int diaSemanaNumerico = diaDaSemanaSolicitado.getValue() % 7;

        Set<Integer> diasDeTrabalho = disponibilidadeRepository.findByMedicoId(medicoId).stream()
                .map(Disponibilidade::getDiaSemana)
                .collect(Collectors.toSet());

        if (!diasDeTrabalho.contains(diaSemanaNumerico)) {
            throw new BadRequest("O médico não atende neste dia da semana.");
        }

        // --- ETAPA 2: O horário está dentro do expediente? ---
        LocalTime horaSolicitada = agendamentoNoFuso.toLocalTime(); // Ex: 14:30:00
        LocalTime fimDoAgendamento = horaSolicitada.plusMinutes(30);

        System.out.println("Hora solicitada: " + horaSolicitada);
        System.out.println("Hora final do agendamentp: " + fimDoAgendamento.toString());

        boolean noPeriodoDaManha = !horaSolicitada.isBefore(medico.getHoraEntrada()) &&
                !fimDoAgendamento.isAfter(medico.getHoraPausa());

        System.out.println("No período da manhã? " + noPeriodoDaManha);

        boolean noPeriodoDaTarde = !horaSolicitada.isBefore(medico.getHoraVolta()) &&
                !fimDoAgendamento.isAfter(medico.getHoraSaida());

        System.out.println("No período da tarde? " + noPeriodoDaTarde);


        if (!noPeriodoDaManha && !noPeriodoDaTarde) {
            throw new BadRequest("O horário solicitado está fora do expediente do médico.");
        }

        // (Opcional) Valida se o horário é "quebrado" (ex: 14:15)
        if (horaSolicitada.getMinute() != 0 && horaSolicitada.getMinute() != 30) {
            throw new BadRequest("Os agendamentos devem ser em intervalos de 30 minutos (ex: 14:00 ou 14:30).");
        }


        // --- ETAPA 3: O slot já está ocupado? ---
        boolean slotOcupado = agendamentoRepository.existsByMedicoIdAndHorarioAndStatusNot(
                medicoId,
                horarioSolicitado, // A verificação no banco é feita em UTC (Instant)
                StatusAgendamento.CANCELADO
        );

        if (slotOcupado) {
            throw new ConflictException("Este horário já está agendado.");
        }
    }

    public ApiResponse<List<AgendamentoResponseDTO>> getAgendamentosByColaborador(String colaboradorId){
        Colaborador colaborador = colaboradorRepository.findById(colaboradorId).orElseThrow(() -> new NotFoundException("colaborador", "Colaborador não encontrado."));

        List<Agendamento> agendamentos = agendamentoRepository.findByColaboradorId(colaboradorId);

        List<AgendamentoResponseDTO> agendamentoResponseDTOs = mapper.toDTOList(agendamentos);

        return new ApiResponse<List<AgendamentoResponseDTO>>(true, agendamentoResponseDTOs, null, null, "Agendamentos encontrados com sucesso!");

    }

    public Page<AgendamentoResponseDTO> getAllAgendamentos(Pageable pageable){
        Page<Agendamento> agendamentoPage = pageAgendamentoRepository.findAll(pageable);

        Page<AgendamentoResponseDTO> agendamentoResponseDTOSPage = agendamentoPage.map(agendamento -> mapper.toDTO(agendamento));

        return agendamentoResponseDTOSPage;
    }

}
