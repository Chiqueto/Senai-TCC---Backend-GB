package com.senai.gestao_beneficios.service.medico;

import com.senai.gestao_beneficios.DTO.disponibilidade.DisponibilidadeMapper;
import com.senai.gestao_beneficios.DTO.medico.MedicoAvaiabilityDTO;
import com.senai.gestao_beneficios.DTO.medico.MedicoMapper;
import com.senai.gestao_beneficios.DTO.medico.MedicoRequestDTO;
import com.senai.gestao_beneficios.DTO.medico.MedicoResponseDTO;
import com.senai.gestao_beneficios.DTO.reponsePattern.ApiResponse;
import com.senai.gestao_beneficios.domain.agendamento.Agendamento;
import com.senai.gestao_beneficios.domain.medico.Disponibilidade;
import com.senai.gestao_beneficios.domain.medico.Especialidade;
import com.senai.gestao_beneficios.domain.medico.Medico;
import com.senai.gestao_beneficios.infra.exceptions.AlreadyExistsException;
import com.senai.gestao_beneficios.infra.exceptions.BadRequest;
import com.senai.gestao_beneficios.infra.exceptions.NotFoundException;
import com.senai.gestao_beneficios.repository.AgendamentoRepository;
import com.senai.gestao_beneficios.repository.DisponibilidadeRepository;
import com.senai.gestao_beneficios.repository.EspecialidadeRepository;
import com.senai.gestao_beneficios.repository.MedicoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MedicoService {
    private final MedicoRepository medicoRepository;
    private final MedicoMapper medicoMapper;
    private final EspecialidadeRepository especialidadeRepository;
    private final DisponibilidadeRepository disponibilidadeRepository;
    private final AgendamentoRepository agendamentoRepository;
    private static final ZoneId FUSO_HORARIO_NEGOCIO = ZoneId.of("America/Sao_Paulo");
    private final DisponibilidadeMapper disponibilidadeMapper;

    public ApiResponse<MedicoResponseDTO> createMedico (MedicoRequestDTO request){
        Optional<Medico> medicoExist = medicoRepository.findByEmail(request.email());

        if (medicoExist.isPresent()){
            throw new AlreadyExistsException("Médico já existe com esse e-mail");
        }

        boolean existeValorInvalido = request.disponibilidade().stream()
                .anyMatch(dia -> dia < 0 || dia > 6);

        if (existeValorInvalido) {
            throw new BadRequest("Valor de disponibilidade inválido. Os valores devem estar entre 0 e 6.");
        }

        Especialidade especialidade = especialidadeRepository.findById(request.id_especialidade()).orElseThrow(
                () -> new NotFoundException("especialidade", "Especialidade não encontrada"));

        Medico medico = new Medico();
        medico.setNome(request.nome());
        medico.setEmail(request.email());
        medico.setEspecialidade(especialidade);
        medico.setHoraEntrada(request.horaEntrada());
        medico.setHoraPausa(request.horaPausa());
        medico.setHoraVolta(request.horaVolta());
        medico.setHoraSaida(request.horaSaida());
        medico.setCreated_at(Instant.now());

        Medico medicoCriado = medicoRepository.save(medico);

        request.disponibilidade().forEach(dia -> {
            Disponibilidade novaDisponibilidade = new Disponibilidade();
            novaDisponibilidade.setMedico(medico);
            novaDisponibilidade.setDiaSemana(dia);
            disponibilidadeRepository.save(novaDisponibilidade);
        });

        MedicoResponseDTO medicoResponseDTO = medicoMapper.toDTO(medicoCriado);
        return new ApiResponse<MedicoResponseDTO>(true, medicoResponseDTO, null, null, "Médico criado com sucesso!");
    }

    public ApiResponse<List<MedicoResponseDTO>> getMedicos () {
        List<Medico> medicos= medicoRepository.findAll();

        List<MedicoResponseDTO> medicosDTO = medicoMapper.toDTOList(medicos);

        return new ApiResponse<>(true, medicosDTO, null, null, "Médicos retornados com sucesso!");
    }

    public ApiResponse<List<MedicoAvaiabilityDTO>> buscarDisponibilidade(String idMedico, LocalDate dia){

        // --- 1. BUSCA O MÉDICO E SUAS DISPONIBILIDADES ---
        Medico medico = medicoRepository.findById(idMedico)
                .orElseThrow(() -> new NotFoundException("medico", "Médico não encontrado"));

        // --- 2. VALIDA O DIA DA SEMANA (CLÁUSULA DE GUARDA) ---
        DayOfWeek diaDaSemana = dia.getDayOfWeek();
        // Supondo que seu mapper converta MONDAY para 1, TUESDAY para 2, ..., SUNDAY para 0
        int diaParaChecar = disponibilidadeMapper.toInteger(diaDaSemana);

        boolean medicoTrabalhaNoDia = medico.getDisponibilidade().stream()
                .anyMatch(disponibilidade -> disponibilidade.getDiaSemana() == diaParaChecar);

        System.out.println("dia: " + dia);
        System.out.println("dia da semana: " + diaDaSemana);
        System.out.println("dia para checar: " + diaParaChecar);
        System.out.println("Disponibilidade: " + medico.getDisponibilidade());

        System.out.println("Médico trabalha no dia: " + medicoTrabalhaNoDia);

        // Se o médico não trabalha no dia solicitado, interrompe a execução aqui.
        if (!medicoTrabalhaNoDia) {
            throw new BadRequest("O médico não atende neste dia da semana!");
        }

        // --- 3. BUSCA OS AGENDAMENTOS JÁ EXISTENTES (SÓ SE O DIA FOR VÁLIDO) ---
        Instant inicioDoDia = dia.atStartOfDay(FUSO_HORARIO_NEGOCIO).toInstant();
        Instant fimDoDia = dia.plusDays(1).atStartOfDay(FUSO_HORARIO_NEGOCIO).toInstant();

        List<Agendamento> agendamentosDoDia = agendamentoRepository
                .findByMedicoIdAndHorarioBetween(idMedico, inicioDoDia, fimDoDia);

        Set<Instant> horariosOcupados = agendamentosDoDia.stream()
                .map(Agendamento::getHorario)
                .collect(Collectors.toSet());

        // --- 4. GERA A LISTA DE SLOTS ---
        List<MedicoAvaiabilityDTO> disponibilidadeDoDia = new ArrayList<>();

        LocalTime slotAtual = medico.getHoraEntrada();
        LocalTime fimDoExpediente = medico.getHoraSaida();

        while (!slotAtual.equals(fimDoExpediente)) { // Usando .equals para mais robustez

            Instant slotEmUtc = dia.atTime(slotAtual).atZone(FUSO_HORARIO_NEGOCIO).toInstant();

            boolean ehHorarioAlmoco = !slotAtual.isBefore(medico.getHoraPausa()) &&
                    slotAtual.isBefore(medico.getHoraVolta());

            boolean estaOcupado = horariosOcupados.contains(slotEmUtc);

            // Lógica de disponibilidade simplificada e correta
            boolean estaDisponivel = !ehHorarioAlmoco && !estaOcupado;

            disponibilidadeDoDia.add(new MedicoAvaiabilityDTO(slotEmUtc, estaDisponivel));

            slotAtual = slotAtual.plusMinutes(30);
        }

        // --- 5. RETORNA A RESPOSTA ---
        return new ApiResponse<>(true, disponibilidadeDoDia, null, null, "Disponibilidade do médico recuperada com sucesso.");
    }


}
