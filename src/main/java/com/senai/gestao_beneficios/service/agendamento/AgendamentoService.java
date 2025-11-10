package com.senai.gestao_beneficios.service.agendamento;

import com.senai.gestao_beneficios.DTO.agendamento.*;
import com.senai.gestao_beneficios.DTO.reponsePattern.ApiResponse;
import com.senai.gestao_beneficios.domain.agendamento.Agendamento;
import com.senai.gestao_beneficios.domain.agendamento.StatusAgendamento;
import com.senai.gestao_beneficios.domain.colaborador.Colaborador;
import com.senai.gestao_beneficios.domain.colaborador.Funcao;
import com.senai.gestao_beneficios.domain.dependente.Dependente;
import com.senai.gestao_beneficios.domain.medico.Disponibilidade;
import com.senai.gestao_beneficios.domain.medico.Medico;
import com.senai.gestao_beneficios.infra.exceptions.*;
import com.senai.gestao_beneficios.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.*;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.senai.gestao_beneficios.specifications.AgendamentoSpecification.*;

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
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Colaborador userLogado = (Colaborador) authentication.getPrincipal();

        Colaborador colaborador = colaboradorRepository.findById(requestDTO.idColaborador()).orElseThrow(() -> new NotFoundException("colaborador", "colaborador não encontrado"));

        if (!colaborador.getId().equals(userLogado.getId())) {
            throw new UnauthorizedException("Você não tem permissão para criar agendamentos para outros colaboradores.");
        }

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

    public Page<AgendamentoResponseDTO> getAllAgendamentos(String colaboradorId, StatusAgendamento status, Pageable pageable){
        Specification<Agendamento> spec = Specification
                .allOf(comColaboradorId(colaboradorId))
                .and(comStatus(status));

        Page<Agendamento> agendamentoPage = pageAgendamentoRepository.findAll(spec, pageable);

        Page<AgendamentoResponseDTO> agendamentoResponseDTOSPage = agendamentoPage.map(agendamento -> mapper.toDTO(agendamento));

        return agendamentoResponseDTOSPage;
    }

    public ApiResponse<AgendamentoResponseDTO> updateAgendamentoDate(AgendamentoDayChangeDTO agendamentoDayChangeDTO, String idAgendamento){
        Agendamento agendamento = agendamentoRepository.findById(idAgendamento).orElseThrow(() -> new NotFoundException("agendamento", "Agendamento não encontrado"));

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Colaborador colaboradorLogado = (Colaborador) authentication.getPrincipal();

        if(!colaboradorLogado.getId().equals(agendamento.getColaborador().getId()) && colaboradorLogado.getFuncao() != Funcao.GESTAO_BENEFICIOS){
            throw new ForbiddenException("Você não tem permissão para alterar agendamentos de outros colaboradores!");
        }

        LocalDate hoje = LocalDate.now(FUSO_HORARIO_NEGOCIO);

        LocalDate diaDoAgendamento = agendamento.getHorario().atZone(FUSO_HORARIO_NEGOCIO).toLocalDate();

        System.out.println("Horario local: " + hoje);

        System.out.println("Dia do agendamento: " + diaDoAgendamento);

        if (!hoje.isBefore(diaDoAgendamento)) {
            throw new BadRequest("Você só pode alterar agendamentos com pelo menos 1 dia de antecedência (até a meia-noite do dia anterior).");
        }

        Instant novoHorario = agendamentoDayChangeDTO.horario();
        String idMedico = agendamento.getMedico().getId();

        verificarDisponibilidade(idMedico, novoHorario);


        agendamento.setHorario(novoHorario);


        Agendamento agendamentoAtualizado = agendamentoRepository.save(agendamento);


        AgendamentoResponseDTO responseDTO = mapper.toDTO(agendamentoAtualizado);

        return new ApiResponse<>(true, responseDTO, null, null, "Agendamento reagendado com sucesso!");
    }

    public ApiResponse<AgendamentoResponseDTO> changeAgendamentoStatus(AgendamentoStatusChangeDTO agendamentoStatusChangeDTO, String idAgendamento){
        Agendamento agendamento = agendamentoRepository.findById(idAgendamento).orElseThrow(() -> new NotFoundException("agendamento", "Agendamento não encontrado"));

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        Colaborador colaborador =  (Colaborador) authentication.getPrincipal();

        if (!agendamento.getColaborador().getMatricula().equals(colaborador.getMatricula()) && colaborador.getFuncao() != Funcao.GESTAO_BENEFICIOS){
            throw new ForbiddenException("Você não tem permissão para alterar agendamento dos outros!");
        }

        if (!agendamentoStatusChangeDTO.status().equals(StatusAgendamento.FALTOU) || !agendamentoStatusChangeDTO.equals(StatusAgendamento.AGENDADO)) {

            Instant agora = Instant.now();
            Instant horarioAgendamento = agendamento.getHorario();

            Instant limiteParaAlteracao = agora.plus(Duration.ofHours(24));

            if (horarioAgendamento.isBefore(limiteParaAlteracao)) {
                throw new ForbiddenException("Agendamentos só podem ser alterados ou cancelados com no mínimo 24 horas de antecedência.");
            }
        }

        agendamento.setStatus(agendamentoStatusChangeDTO.status());

        Agendamento agendamentoAtualizado = agendamentoRepository.save(agendamento);

        AgendamentoResponseDTO response = mapper.toDTO(agendamentoAtualizado);

        return new ApiResponse<>(true, response, null, null, "Status alterado com sucesso!");
    }

}
